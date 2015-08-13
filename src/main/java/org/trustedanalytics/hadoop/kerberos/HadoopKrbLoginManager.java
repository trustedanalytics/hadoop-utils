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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Utils for Kerberos authentication using credentials or keytab method.
 */
final class HadoopKrbLoginManager implements KrbLoginManager {

  static final String KRB5_KDC = "java.security.krb5.kdc";

  static final String KRB5_REALM = "java.security.krb5.realm";

  static final String KRB5_CONF = "java.security.krb5.conf";

  private static final String KERB_CACHE_NAME = "KRB5CCNAME";

  private static final String KERB_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

  private FactoryHelper helper;

  HadoopKrbLoginManager(String kdc, String defaultRealm, FactoryHelper helper) {
    validateParams(kdc, defaultRealm);
    initKerberos(kdc, defaultRealm);
    this.helper = helper;
  }

  HadoopKrbLoginManager(String kdc, String defaultRealm) {
    this(kdc, defaultRealm, new FactoryHelper());
  }

  static void validateParams(String kdcParam, String defaultRealmParam) {
    Preconditions
        .checkArgument(!Strings.isNullOrEmpty(kdcParam), "KDC address cannot be empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultRealmParam),
                                "Default realm cannot be empty");
  }

  @Override
  public Subject loginWithCredentials(String user, char[] password) throws LoginException {
    setKerbConfigFromOpts(getDefaultOptionsForPrincipal(user));
    LoginContext lc = helper.getLoginContext(KERB_MODULE, new FixedPasswordHandler(password));
    return loginWithKerberos(lc);
  }

  @Override
  public Subject loginWithKeyTab(String user, String path) throws LoginException {
    setKerbConfigFromOpts(getKeyTabOptionsForPrincipal(user, path));
    return loginWithKerberos(helper.getLoginContext(KERB_MODULE));
  }

  @Override
  public void loginInHadoop(Subject subject, org.apache.hadoop.conf.Configuration hadoopConf)
      throws IOException {
    UserGroupInformation.setConfiguration(hadoopConf);
    UserGroupInformation.loginUserFromSubject(subject);
  }

  private void initKerberos(String kdc, String defaultRealm) {
    String confFile = System.getProperty(KRB5_CONF);
    if (confFile == null || confFile.isEmpty()) {
      System.setProperty(KRB5_KDC, kdc);
      System.setProperty(KRB5_REALM, defaultRealm);
    }
  }

  private static Map<String, String> getKeyTabOptionsForPrincipal(String user, String path) {
    Map<String, String> opts = getDefaultOptionsForPrincipal(user);
    opts.put("keyTab", path);
    opts.put("useKeyTab", "true");
    return opts;
  }

  private static Subject loginWithKerberos(LoginContext lc) throws LoginException {
    lc.login();
    return lc.getSubject();
  }

  public void setKerbConfigFromOpts(Map<String, String> opts) {
    AppConfigurationEntry[] appConfigurationEntry =
        new AppConfigurationEntry[]{new AppConfigurationEntry(KERB_MODULE,
                                                              LoginModuleControlFlag.REQUIRED,
                                                              opts)};
    // TODO: InMemory... brings spring dependency. Replace with own implementation
    Configuration.setConfiguration(new InMemoryConfiguration(appConfigurationEntry));
  }


  private static Map<String, String> getDefaultOptionsForPrincipal(String principal) {
    Map<String, String> options = new HashMap<>();

    options.put("principal", principal);
    options.put("storeKey", "true");
    options.put("doNotPrompt", "false");
    options.put("useTicketCache", "true");
    options.put("renewTGT", "true");
    options.put("refreshKrb5Config", "true");
    options.put("isInitiator", "true");
    options.put("clearPass", "false");

    String ticketCache = System.getenv(KERB_CACHE_NAME);
    if (ticketCache != null) {
      options.put("ticketCache", ticketCache);
    }
    options.put("debug", "true");
    return options;
  }

  static final class FixedPasswordHandler implements CallbackHandler {

    private char[] password;

    public FixedPasswordHandler(char[] password) {
      this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

      for (Callback callback : callbacks) {
        if (callback instanceof PasswordCallback) {
          ((PasswordCallback) callback).setPassword(password);
        } else {
          throw new UnsupportedCallbackException(callback);
        }
      }
    }
  }

  static class FactoryHelper {

    LoginContext getLoginContext(String module, CallbackHandler handler) throws LoginException {
      return new LoginContext(module, handler);
    }

    LoginContext getLoginContext(String module) throws LoginException {
      return new LoginContext(module);
    }
  }

}
