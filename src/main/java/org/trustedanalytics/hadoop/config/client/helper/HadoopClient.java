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
package org.trustedanalytics.hadoop.config.client.helper;

import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.JwtToken;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import java.io.IOException;
import java.util.Optional;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

class HadoopClient {

  private static final String AUTHENTICATION_METHOD = "kerberos";

  private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

  private KrbLoginManager loginManager;

  private ServiceInstanceConfiguration serviceConfiguration;

  private ServiceInstanceConfiguration krbServiceConfiguration;

  private HadoopClient() {
  }

  /**
   * {@inheritDoc}
   */
  public Configuration createConfig() throws LoginException, IOException {
    Configuration hadoopConf = this.serviceConfiguration.asHadoopConfiguration();
    if (isKerberosEnabled(hadoopConf)) {
      loginManager.loginInHadoop(getLoggedUserIdentity(), hadoopConf);
    }
    return hadoopConf;
  }

  /**
   * {@inheritDoc}
   */
  public Configuration createConfig(JwtToken jwtToken) throws LoginException, IOException {
    throw new UnsupportedOperationException("Not implemented, yet!");
  }

  /**
   * {@inheritDoc}
   */
  public boolean isKerberosEnabled(Configuration hadoopConf) {
    return AUTHENTICATION_METHOD.equals(hadoopConf.get(AUTHENTICATION_METHOD_PROPERTY));
  }

  Subject getLoggedUserIdentity() throws LoginException {
    String userName = getKrbServiceProperty(Property.USER);
    String pass = getKrbServiceProperty(Property.PASSWORD);
    return this.loginManager.loginWithCredentials(userName, pass.toCharArray());
  }

  String getServiceProperty(Property property) {
    return this.serviceConfiguration.getProperty(property)
        .orElseThrow(() -> new IllegalStateException(property.name()
                                                     + " not found in configuration!"));
  }

  String getKrbServiceProperty(Property property) {
    return this.krbServiceConfiguration.getProperty(property)
        .orElseThrow(() -> new IllegalStateException(property.name()
                                                     + " not found in configuration!"));
  }

  void setLoginManager(Optional<KrbLoginManager> loginManager) {
    this.loginManager = loginManager.orElse(getDefaultLoginManager());
  }

  void setServiceConfiguration(ServiceInstanceConfiguration serviceConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
  }

  void setKrbServiceConfiguration(ServiceInstanceConfiguration krbServiceConfiguration) {
    this.krbServiceConfiguration = krbServiceConfiguration;
  }

  private KrbLoginManager getDefaultLoginManager() {
    String kdc = getKrbServiceProperty(Property.KRB_KDC);
    String realm = getKrbServiceProperty(Property.KRB_REALM);
    return KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(kdc, realm);
  }

  /**
   * Description of how to compose {@link HadoopClient} helper.
   */
  static class Builder {

    private static String KRB_SERVICE_DEFAULT_NAME = "kerberos-service";

    private String serviceName;

    private String krbServiceName;

    private AppConfiguration appConfiguration;

    private KrbLoginManager loginManager;

    private ServiceType serviceType;

    private HadoopClient hadoopClient = new HadoopClient();

    private Builder() {
    }

    public static Builder newInstance() {
      return new Builder();
    }

    public Builder withServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder withKrbServiceName(String krbServiceName) {
      this.krbServiceName = serviceName;
      return this;
    }

    public Builder withAppConfiguration(AppConfiguration appConfiguration) {
      this.appConfiguration = appConfiguration;
      return this;
    }

    public Builder withLoginManager(KrbLoginManager loginManager) {
      this.loginManager = loginManager;
      return this;
    }

    public Builder withServiceType(ServiceType serviceType) {
      this.serviceType = serviceType;
      return this;
    }

    public Optional<String> getServiceName() {
      return Optional.ofNullable(serviceName);
    }

    public Optional<AppConfiguration> getAppConfiguration() {
      return Optional.ofNullable(appConfiguration);
    }

    public Optional<String> getKrbServiceName() {
      return Optional.ofNullable(krbServiceName);
    }

    public Optional<KrbLoginManager> getLoginManager() {
      return Optional.ofNullable(loginManager);
    }

    public Optional<ServiceType> getServiceType() {
      return Optional.ofNullable(this.serviceType);
    }

    public HadoopClient build() {
      AppConfiguration conf = getAppConfiguration()
          .orElseGet(() -> {
            try {
              return Configurations.newInstanceFromEnv();
            } catch (IOException ignore) {
              throw new IllegalStateException("Unable to read configuration. Environment variable "
                                              + ConfigConstants.VCAP_SERVICES +
                                              " is probably not correctly set.",
                                              ignore);
            }
          });
      getServiceType().
          ifPresent(sType -> this.hadoopClient.setServiceConfiguration(conf.getServiceConfig(sType)));
      getServiceName().
          ifPresent(serviceName ->
                        this.hadoopClient.setServiceConfiguration(conf.getServiceConfig(serviceName)));

      this.hadoopClient.setKrbServiceConfiguration(
          conf.getServiceConfig(getKrbServiceName().orElse(KRB_SERVICE_DEFAULT_NAME)));
      this.hadoopClient.setLoginManager(getLoginManager());
      return this.hadoopClient;
    }
  }
}