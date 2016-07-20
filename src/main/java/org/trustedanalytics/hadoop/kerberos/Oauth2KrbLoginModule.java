/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.hadoop.kerberos;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import com.sun.security.auth.module.Krb5LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;
import org.trustedanalytics.hadoop.config.client.oauth.TapOauthToken;

import sun.security.krb5.PrincipalName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public final class Oauth2KrbLoginModule implements LoginModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2KrbLoginModule.class);

  private LoginModule delegate;

  private String ticketCache;

  private String ktinit;

  public Oauth2KrbLoginModule() {
    delegate = new Krb5LoginModule();
  }

  @Override
  public void initialize(Subject subject, CallbackHandler callbackHandler,
                         Map<String, ?> sharedState, Map<String, ?> options) {
    Map<String, ?> optionsToDelegee = options;
    if (ConfigOptions.USE_TOKEN.asBoolean(options)) {
      try {
        JwtToken tapToken = retrieveToken(callbackHandler, options);
        ConfigOptions.TICKET_CACHE.asString(options)
            .map(cache ->this.ticketCache = cache)
            .orElseGet(() -> this.ticketCache =
                HadoopKrbLoginManager.ticketCacheLocation(getPrincipalName(tapToken)));
        this.ktinit  = ConfigOptions.KTINIT_COMMAND.asString(options)
            .orElse(System.getProperty("user.dir") + "/krb5jwt/bin/ktinit");
        prepareKrbCCache(tapToken);
        optionsToDelegee = prepareOptionsForDelegation(tapToken, options);
      } catch (IOException | UnsupportedCallbackException | LoginException e) {
        throw new IllegalStateException("Can't initialize "
                                        + Oauth2KrbLoginModule.class.getName() + "!", e);
      }
    }
    delegate.initialize(subject,
                        callbackHandler,
                        sharedState,
                        optionsToDelegee);
  }

  @Override
  public boolean login() throws LoginException {
    return delegate.login();
  }

  @Override
  public boolean commit() throws LoginException {
    return delegate.commit();
  }

  @Override
  public boolean abort() throws LoginException {
    return delegate.abort();
  }

  @Override
  public boolean logout() throws LoginException {
    return delegate.logout();
  }

  /**
   * Prepare kerberos cache credentials based on Oauth2 token.
   *
   * @param jwtToken ouath2 token
   * @throws LoginException
   */
  synchronized void prepareKrbCCache(JwtToken jwtToken) throws LoginException {
    Preconditions.checkNotNull(ticketCache, "Ticket cache location not set!");
    String kinitCmd = String.format("%s -t %s -c %s -P %s",
                                    this.ktinit,
                                    jwtToken.getRawToken(),
                                    ticketCache,
                                    getPrincipalName(jwtToken));
    Runtime run = Runtime.getRuntime();
    try {
      Process pr = run.exec(kinitCmd);
      pr.waitFor();
      try (BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
        buf.lines().forEach(LOGGER::info);
      }
      if (pr.exitValue() != 0) {
        try(BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()))) {
          StringBuilder toLog = new StringBuilder("ktinit execution failed: \n");
          err.lines().forEach(line -> toLog.append(line).append("\n"));
          throw new LoginException(toLog.toString());
        }
      }
      if(!Files.exists(Paths.get(ticketCache))) {
        throw new LoginException("Failed to create krb credential cache in location: "
                                 + ticketCache);
      }
    } catch (IOException | InterruptedException e) {
      LoginException propagate = new LoginException(e.getMessage());
      propagate.initCause(e);
      throw propagate;
    }
  }

  @SuppressWarnings("unchecked")
  Map<String, String> prepareOptionsForDelegation(JwtToken token, Map optionsFromConfig) {
    Map<String, String> options = Maps.newHashMap(optionsFromConfig);
    options.put("principal", token.getUserId());
    options.putIfAbsent("storeKey", "false");
    options.putIfAbsent("doNotPrompt", "true");
    options.putIfAbsent("useTicketCache", "true");
    options.putIfAbsent("renewTGT", "true");
    options.putIfAbsent("refreshKrb5Config", "true");
    options.putIfAbsent("isInitiator", "true");
    options.putIfAbsent("clearPass", "false");
    options.putIfAbsent("ticketCache", this.ticketCache);
    return options;
  }

  String getPrincipalName(JwtToken token) {
    Preconditions.checkNotNull(token);
    return token.getUserId()
           + PrincipalName.NAME_REALM_SEPARATOR_STR
           + System.getProperty(HadoopKrbLoginManager.KRB5_REALM);
  }

  /**
   * Retrieve oauth2 token for authentication process.
   *
   * @param callbackHandler logic used to get token passed from configuration.
   * @param options module configuration
   *
   * @return oauth2 token
   * @throws IOException
   * @throws UnsupportedCallbackException
   */
  JwtToken retrieveToken(CallbackHandler callbackHandler, Map<String, ?> options)
      throws IOException, UnsupportedCallbackException {
    Preconditions.checkNotNull(callbackHandler, "CallbackHandler must be set in LoginContext! "
                                                + "Try to set auth.login.defaultCallbackHandler "
                                                + "security property.");
    Preconditions.checkNotNull(options);
    Callback[] callbacks = new Callback[1];
    //if token cache location is set, we use FromFileTokenRetriver.
    ConfigOptions.TOKEN_CACHE.asString(options).ifPresent(
        cacheLocation -> callbacks[0] =
            new Oauth2TokenCallback(new FromFileTokenRetriver(cacheLocation)));
    callbackHandler.handle(callbacks);
    Supplier<String> tokenRetriver = ((Oauth2TokenCallback) callbacks[0]).tokenRetriever();
    return new TapOauthToken(tokenRetriver.get());
  }

  public enum ConfigOptions {

    USE_TOKEN("useToken"),
    TOKEN_CACHE("tokenCache"),
    TICKET_CACHE("ticketCache"),
    KTINIT_COMMAND("ktinitCommand");

    private String name;

    ConfigOptions(String name) {
      this.name = name;
    }

    public Optional<String> asString(Map<String, ?> options) {
      return Optional.ofNullable((String) options.get(name));
    }

    public boolean asBoolean(Map<String, ?> options) {
      return Boolean.parseBoolean((String) options.get(name));
    }

    public String getName() {
      return this.name;
    }
  }
}