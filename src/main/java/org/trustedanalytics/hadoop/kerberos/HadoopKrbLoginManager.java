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
import com.google.common.base.Strings;

import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;
import org.trustedanalytics.hadoop.config.client.oauth.TapOauthToken;

import sun.security.krb5.KrbAsReqBuilder;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.ccache.Credentials;
import sun.security.krb5.internal.ccache.CredentialsCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.kerberos.KeyTab;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Utils for Kerberos authentication using credentials or keytab method.
 */
final class HadoopKrbLoginManager implements KrbLoginManager {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(HadoopKrbLoginManager.class);

  static final String KRB5_KDC = "java.security.krb5.kdc";

  static final String KRB5_REALM = "java.security.krb5.realm";

  static final String KRB5_KINIT_CMD_PROP_NAME = "hadoop.kerberos.kinit.command";

  static final  String KRB5_USE_SUBJECT_CREDS_LIMITATION = "javax.security.auth.useSubjectCredsOnly";

  static final String KRB5_CONF = "java.security.krb5.conf";

  static final String KRB5_TGT_PRINCIPAL_NAME = "krbtgt";

  private static final String KERB_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

  private static final String KRB5_CREDENTIALS_CACHE_DIR = "/tmp/";

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
  public Subject loginWithJWTtoken(JwtToken jwtToken) throws LoginException {
    String userId = jwtToken.getUserId();
    setKerbConfigFromOpts(userId, getDefaultOptionsForPrincipal(userId));
    LoginContext lc = helper.getLoginContext(userId,
                                             new FixedPasswordHandler("some".toCharArray()));
    helper.cacheKrbCredentials(jwtToken);
    return login(lc);
  }

  @Override
  public Subject loginWithCredentials(String user, char[] password) throws LoginException {
    setKerbConfigFromOpts(user, getDefaultOptionsForPrincipal(user));
    LoginContext lc = helper.getLoginContext(user, new FixedPasswordHandler(password));
    helper.cacheKrbCredentials(user, password);
    return login(lc);
  }

  @Override
  public Subject loginWithKeyTab(String user, String path) throws LoginException {
    setKerbConfigFromOpts(user, getKeyTabOptionsForPrincipal(user, path));
    LoginContext lc = helper.getLoginContext(user);
    helper.cacheKrbCredentials(user, path);
    return login(lc);
  }

  @Override
  public void loginInHadoop(Subject subject, org.apache.hadoop.conf.Configuration hadoopConf)
      throws IOException {
    Preconditions.checkNotNull(subject, "Subject can't be null!");
    Preconditions.checkNotNull(hadoopConf, "Hadoop configuration can't be null!");

    String ccLocation = ticketCacheLocation(subject);
    hadoopConf.set(CommonConfigurationKeys.KERBEROS_TICKET_CACHE_PATH, ccLocation);
    hadoopConf.set(KRB5_KINIT_CMD_PROP_NAME, "kinit -c " + ccLocation);
    getUGI(subject);
    UserGroupInformation.setConfiguration(hadoopConf);
  }

  @Override
  public UserGroupInformation getUGI(Subject subject) throws IOException {
    Preconditions.checkNotNull(subject, "Subject can't be null!");
    return UserGroupInformation.getBestUGI(ticketCacheLocation(subject), getPrincipalName(subject));
  }

  static String getPrincipalName(Subject subject) {
    Preconditions.checkNotNull(subject, "Subject can't be null!");
    Preconditions.checkArgument(!subject.getPrincipals().isEmpty(),
                                "Can't find any principal in given Subject!");
    return subject.getPrincipals().iterator().next().getName();
  }

  public static String getUserName(String jwtToken) {
    JwtToken token = new TapOauthToken(jwtToken);
    return token.getUserName();
  }

  public static String getUserId(String jwtToken) {
    JwtToken token = new TapOauthToken(jwtToken);
    return token.getUserId();
  }

  String ticketCacheLocation(Subject subject) {
    return ticketCacheLocation(getPrincipalName(subject));
  }

  static String ticketCacheLocation(String princName) {
    return KRB5_CREDENTIALS_CACHE_DIR
           + princName.replace(PrincipalName.NAME_COMPONENT_SEPARATOR, '_');
  }

  private void initKerberos(String kdc, String defaultRealm) {
    System.setProperty(KRB5_KDC, kdc);
    System.setProperty(KRB5_REALM, defaultRealm);
    System.setProperty(KRB5_USE_SUBJECT_CREDS_LIMITATION, "false");
    Configuration.setConfiguration(InMemoryMultiuserJaasConfiguration.getInstance());
  }

  private static Map<String, String> getKeyTabOptionsForPrincipal(String user, String path) {
    Map<String, String> opts = getDefaultOptionsForPrincipal(user);
    opts.put("keyTab", path);
    opts.put("useKeyTab", "true");
    return opts;
  }

  private Subject login(LoginContext lc) throws LoginException {
    lc.login();
    return lc.getSubject();
  }

  public void setKerbConfigFromOpts(String username, Map<String, String> opts) {
    AppConfigurationEntry[] appConfigurationEntry =
        new AppConfigurationEntry[]{new AppConfigurationEntry(KERB_MODULE,
                                                              LoginModuleControlFlag.REQUIRED,
                                                              opts)};
    InMemoryMultiuserJaasConfiguration conf =
        (InMemoryMultiuserJaasConfiguration) Configuration.getConfiguration();
    conf.append(username, appConfigurationEntry);
  }

  private static Map<String, String> getDefaultOptionsForPrincipal(String user) {
    Map<String, String> options = new HashMap<>();
    LOGGER.debug("Using principal name : " + user);
    options.put("principal", user);
    options.put("storeKey", "false");
    options.put("doNotPrompt", "false");
    options.put("useTicketCache", "true");
    options.put("renewTGT", "true");
    options.put("refreshKrb5Config", "true");
    options.put("isInitiator", "true");
    options.put("clearPass", "false");
    options.put("ticketCache", ticketCacheLocation(user + PrincipalName.NAME_REALM_SEPARATOR_STR
                               + System.getProperty(KRB5_REALM)));
    options.put("debug", "true");
    return options;
  }

  static final class FixedPasswordHandler implements CallbackHandler {

    private final char[] password;

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

    synchronized void cacheKrbCredentials(String user, char[] pass) throws LoginException {
      try {
        PrincipalName pName = new PrincipalName(user, PrincipalName.KRB_NT_PRINCIPAL);
        getTgt(pName, prepareTgtReq(pName, pass));
      } catch (KrbException | IOException e) {
        LoginException propagate = new LoginException(e.getMessage());
        propagate.initCause(e);
        throw propagate;
      }
    }

    synchronized void cacheKrbCredentials(String user, String keyTabPath) throws LoginException {
      try {
        PrincipalName pName = new PrincipalName(user, PrincipalName.KRB_NT_PRINCIPAL);
        getTgt(pName, prepareTgtReq(pName, keyTabPath));
      } catch (KrbException | IOException e) {
        LoginException propagate = new LoginException(e.getMessage());
        propagate.initCause(e);
        throw propagate;
      }
    }

    synchronized void cacheKrbCredentials(JwtToken jwtToken) throws LoginException {
      String princName = jwtToken.getUserId() +
                         PrincipalName.NAME_REALM_SEPARATOR_STR +
                         System.getProperty(KRB5_REALM);
      String path = System.getProperty("user.dir");
      String kinit = path + "/krb5jwt/bin/ktinit -t " + jwtToken.getRawToken()
                     + " -c " + HadoopKrbLoginManager.ticketCacheLocation(princName)
                     + " -P " + princName;
      Runtime run = Runtime.getRuntime();
      try {
        Process pr = run.exec(kinit);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        buf.lines().forEach(LOGGER::info);
        if (pr.exitValue() != 0) {
          StringBuilder toLog = new StringBuilder("ktinit execution failed: \n");
          BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
          err.lines().forEach(line -> toLog.append(line).append("\n"));
          err.close();
          throw new LoginException(toLog.toString());
        }
        buf.close();
      } catch (IOException | InterruptedException e) {
        LoginException propagate = new LoginException(e.getMessage());
        propagate.initCause(e);
        throw propagate;
      }
    }

    private KrbAsReqBuilder prepareTgtReq(PrincipalName pName, char[] secret) throws KrbException {
      return new KrbAsReqBuilder(pName, secret);
    }

    private KrbAsReqBuilder prepareTgtReq(PrincipalName pName, String keyTabLocation)
        throws KrbException {
      KeyTab secret = KeyTab.getInstance(new File(keyTabLocation));
      return new KrbAsReqBuilder(pName, secret);
    }

    private void getTgt(PrincipalName pName, KrbAsReqBuilder builder) throws KrbException,
                                                                             IOException {

      PrincipalName krbTGTpName = new PrincipalName(KRB5_TGT_PRINCIPAL_NAME
                                                    + PrincipalName.NAME_COMPONENT_SEPARATOR_STR
                                                    + System.getProperty(KRB5_REALM),
                                                    PrincipalName.KRB_NT_SRV_INST);
      String cCacheLocation = "FILE:"
                              + ticketCacheLocation(pName.getName());

      KDCOptions kdcOptions = new KDCOptions();
      kdcOptions.set(KDCOptions.FORWARDABLE, true);
      kdcOptions.set(KDCOptions.PROXIABLE, true);
      kdcOptions.set(KDCOptions.RENEWABLE, true);

      builder.setOptions(kdcOptions);
      builder.setTarget(krbTGTpName);
      builder.action();

      Credentials cCreds = builder.getCCreds();
      builder.destroy();
      CredentialsCache cc = CredentialsCache.getInstance(pName, cCacheLocation);
      if (cc == null) {
        LOGGER.debug("Creating new credentials cache file: " + cCacheLocation);
        cc = CredentialsCache.create(pName, cCacheLocation);
      }
      cc.update(cCreds);
      cc.save();
    }
  }
}