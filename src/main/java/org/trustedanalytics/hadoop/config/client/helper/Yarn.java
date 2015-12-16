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

import com.google.common.annotations.VisibleForTesting;

import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * Provides access to yarn client configuration. Applicable to services of type
 * {@link org.trustedanalytics.hadoop.config.client.ServiceType#YARN_TYPE}.
 */
public final class Yarn {

  private final HadoopClient hadoopClient;

  /**
   * Creates new instance of Yarn client helper. Assume that the only one instance of
   * {@link org.trustedanalytics.hadoop.config.client.ServiceType#YARN_TYPE} is bound.
   *
   * @return new instance of Yarn helper
   * @throws IOException
   */
  public static Yarn newInstance() throws IOException {
    return new Yarn(HadoopClient.Builder.newInstance()
                        .withServiceType(ServiceType.YARN_TYPE).build());
  }

  /**
   * Creates new instance of Yarn client helper for Yarn service named instanceName.
   *
   * @param instanceName Yarn service instance name
   * @return new instance of Yarn helper
   * @throws IOException
   */
  public static Yarn newInstance(String instanceName) throws IOException {
    return new Yarn(HadoopClient.Builder.newInstance().withServiceName(instanceName).build());
  }

  @VisibleForTesting
  static Yarn newInstanceForTests(HadoopClient.Builder builder) throws IOException {
    return new Yarn(builder.build());
  }


  private Yarn(HadoopClient defaultClient) throws IOException {
    this.hadoopClient = defaultClient;
  }

  /**
   * Create new {@link Configuration} object.
   *
   * Authenticates (if needed) using password from configuration.
   *
   *  @return configuration
   *  @throws LoginException
   *  @throws IOException
   */
  public Configuration createConfig() throws LoginException, IOException {
    return hadoopClient.createConfig();
  }

  /**
   * Create new {@link Configuration} object.
   *
   * Authenticates using JwtToken.
   *
   * @param jwtToken authentication token
   * @return configuration
   * @throws LoginException
   * @throws IOException
   */
  public Configuration createConfig(JwtToken jwtToken) throws LoginException, IOException {
    return hadoopClient.createConfig(jwtToken);
  }

  /**
   * Checks if authentication method type is set to "Kerberos".
   *
   * @param hadoopConf service configuration
   * @return true if kerberos is set
   */
  public boolean isKerberosEnabled(Configuration hadoopConf) {
    return hadoopClient.isKerberosEnabled(hadoopConf);
  }

  String getServiceProperty(Property property) {
    return this.hadoopClient.getServiceProperty(property);
  }

  String getKrbServiceProperty(Property property) {
    return this.hadoopClient.getKrbServiceProperty(property);
  }

}
