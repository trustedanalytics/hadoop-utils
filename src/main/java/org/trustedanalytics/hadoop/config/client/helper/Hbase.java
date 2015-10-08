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
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.trustedanalytics.hadoop.config.client.JwtToken;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * Provides access to hbase client connection and configuration. Applicable to services of type
 * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HBASE_TYPE}.
 *
 * Usage:
 * 1) Use case with one configured service account.
 *
 * 1.1) How to get hbase client configuration (one hbase service bound)?
 * Configuration hbaseConf = Hbase.newInstance().createConfig();
 *
 * 1.2) How to get hbase connection (one hbase service bound)?
 * Connection hbaseConn = Hbase.newInstance().createConnection();
 *
 * 1.3) How to get hbase configuration for given service instance name?
 * Configuration hbaseConf = Hbase.newInstance("hbase-instance").createConfig();
 *
 * 1.4) How to get hbase connection for given service instance name?
 * Connection hbaseConn = Hbase.newInstance("hbase-instance").createConnection();
 *
 *
 * 2) Use case with user identity from oauth (not yet implemented).
 *
 * 2.1) How to get hbase configuration (one hbase service bound)?
 * JwtToken jwtToken;
 * ...
 * Configuration hbaseConf = Hbase.newInstance().createConfig(jwtToken);
 *
 * 2.2) How to get hbase connection (one hbase service bound)?
 * JwtToken jwtToken;
 * ...
 * Connection hbaseConn = Hbase.newInstance().createConnection(jwtToken);
 *
 * 2.3) How to get hbase configuration for given service instance name?
 * JwtToken jwtToken;
 * ...
 * Configuration hbaseConf = Hbase.newInstance("hbase-instance").createConfig(jwtToken);
 *
 * 2.4) How to get file system object for given service instance name?
 * JwtToken jwtToken;
 * ...
 * Connection hbaseConn = Hbase.newInstance("hbase-instance").createConnection(jwtToken);
 */
public final class Hbase {

  private final HadoopClient hadoopClient;

  /**
   * Creates new instance of Hbase client helper. Assume that the only one instance of
   * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HBASE_TYPE} is bound.
   *
   * @return new instance of Hbase helper
   * @throws IOException
   */
  public static Hbase newInstance() throws IOException {
    return new Hbase(HadoopClient.Builder.newInstance().withServiceType(ServiceType.HBASE_TYPE).build());
  }

  /**
   * Creates new instance of Hbase client helper for hbase service named instanceName.
   *
   * @param instanceName hbase service instance name
   * @return new instance of Hbase helper
   * @throws IOException
   */
  public static Hbase newInstance(String instanceName) throws IOException {
    return new Hbase(HadoopClient.Builder.newInstance().withServiceName(instanceName).build());
  }

  @VisibleForTesting
  static Hbase newInstanceForTests(HadoopClient.Builder builder) throws IOException {
    return new Hbase(builder.build());
  }

  private Hbase(HadoopClient hadoopClient) throws IOException {
    this.hadoopClient = hadoopClient;
  }

  /**
   * Create new hbase {@link Connection} object.
   **
   * @return hbase connection system for service user (creds from app configuration)
   * @throws LoginException, IOException
   */
  public Connection createConnection() throws LoginException, IOException {
    Configuration hbaseConf = HBaseConfiguration.create(hadoopClient.createConfig());
    if (!hadoopClient.isKerberosEnabled(hbaseConf)) {
      return ConnectionFactory.createConnection(hbaseConf);
    }
    User user = UserProvider.instantiate(hbaseConf)
        .create(UserGroupInformation.getUGIFromSubject(hadoopClient.getLoggedUserIdentity()));
    return ConnectionFactory.createConnection(hbaseConf, user);
  }

  /**
   * Create new hbase {@link Connection} object.
   *
   * @param jwtToken oauth token
   * @return hbase file system for user that is identified by jwt token
   */
  public Connection  createConnection(JwtToken jwtToken) {
    throw new UnsupportedOperationException("Not implemented, yet!");
  }

  /**
   * Create new {@link Configuration} object.
   *
   * Authentication (if needed) with the use of password from configuration.
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
   * Authentication with the use of JwtToken from oauth server.
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
   * Check if authentication method is set on kerberos?
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