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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides access to hive client connection and configuration. Applicable to services of type
 * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HIVE_TYPE}.
 *
 * Usage:
 * 1) Use case with one configured service account.
 *
 * 1.1) How to get hive client configuration (one hive service bound)?
 * Configuration hiveConf = Hive.newInstance().createConfig();
 *
 * 1.2) How to get hive connection (one hive service bound).
 * Connection hiveConn = Hive.newInstance().createConnection();
 *
 * 1.3) How to get hive connection (one hive service bound) for specified
 *      database (if bound to multitenant plan).
 * Connection hiveConn = Hive.newInstance().createConnection("database");
 *
 * 1.4) How to get hive configuration for given service instance name.
 * Configuration hiveConf = Hive.newInstance("hive-instance").createConfig();
 *
 * 1.5) How to get hive connection for given service instance name.
 * Connection hiveConn = Hive.newInstance("hive-instance").createConnection();
 *
 * 1.6) How to get hive connection for given service instance name (if bound to multitenant plan).
 * Connection hiveConn = Hive.newInstance("hive-instance").createConnection("database");
 *
 * 2) Use case with user identity from oauth.
 *
 * 2.1) How to get hive configuration (one hive service bound).
 * JwtToken jwtToken;
 * ...
 * Configuration hiveConf = Hive.newInstance().createConfig(jwtToken);
 *
 * 2.2) How to get hive connection (one hive service bound).
 * JwtToken jwtToken;
 * ...
 * Connection hiveConn = Hive.newInstance().createConnection(jwtToken);
 *
 * 1.3) How to get hive connection (one hive service bound) for specified
 *      database (if bound to multitenant plan).
 * JwtToken jwtToken;
 * ...
 * Connection hiveConn = Hive.newInstance().createConnection(jwtToken, "database");
 *
 * 2.4) How to get hive configuration for given service instance name.
 * JwtToken jwtToken;
 * ...
 * Configuration hiveConf = Hive.newInstance("hive-instance").createConfig(jwtToken);
 *
 * 2.5) How to get file system object for given service instance name.
 * JwtToken jwtToken;
 * ...
 * Connection hiveConn = Hive.newInstance("hive-instance").createConnection(jwtToken);
 *
 * 2.6) How to get hive connection for given service instance name (if bound to multitenant plan).
 * JwtToken jwtToken;
 * ...
 * Connection hiveConn = Hive.newInstance("hive-instance").createConnection(jwtToken, "database");
 */
public class Hive {

  private static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

  private final HadoopClient hadoopClient;

  /**
   * Creates new instance of Hive client helper, assuming that the only one instance of
   * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HIVE_TYPE} is bound.
   *
   * @return new instance of Hive helper
   * @throws IOException
   */
  public static Hive newInstance() throws IOException {
    return new Hive(
        HadoopClient.Builder.newInstance().withServiceType(ServiceType.HIVE_TYPE).build());
  }

  /**
   * Creates new instance of Hive client helper for hive service named instanceName.
   *
   * @param instanceName hive service instance name
   * @return new instance of Hive helper
   * @throws IOException
   */
  public static Hive newInstance(String instanceName) throws IOException {
    return new Hive(HadoopClient.Builder.newInstance().withServiceName(instanceName).build());
  }

  @VisibleForTesting
  static Hive newInstanceForTests(HadoopClient.Builder builder) throws IOException {
    return new Hive(builder.build());
  }

  private Hive(HadoopClient hadoopClient) throws IOException {
    this.hadoopClient = hadoopClient;
  }

  private String getRawConnectionString() {
    return getServiceProperty(Property.HIVE_URL);
  }

  /**
   * Create new hive connections string without specified database.
   *
   * @return hive connection string as {@link String} object
   */
  public String getConnectionString() {
    return getConnectionString("");
  }

  /**
   * Create new hive connections string with specified and unified database name.
   *
   * @param organizationId database name
   * @return hive connection string as {@link String} object
   */
  public String getConnectionString(String organizationId) {
    String uri = getRawConnectionString();
    return HdfsPathTemplateUtils.fill(uri, null, organizationId.replace('-','_'));
  }

  /**
   * Create new hive {@link Connection} object without specified database.
   *
   * @return hive connection system for service user (creds from app configuration)
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public Connection getConnection()
      throws LoginException, IOException, InterruptedException, URISyntaxException {
    return getConnection("");
  }

  /**
   * Create new hive {@link Connection} object without specified database.
   *
   * @param jwtToken oauth token
   * @return hive connection system for user that is identified by jwt token
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public Connection getConnection(JwtToken jwtToken)
      throws LoginException, IOException, InterruptedException, URISyntaxException {
    return getConnection(jwtToken, "");
  }

  /**
   * Create new hive {@link Connection} object with specified and unified database name.
   *
   * @param database database name
   * @return hive connection system for service user (creds from app configuration)
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public Connection getConnection(String database)
      throws LoginException, IOException, InterruptedException, URISyntaxException {
    Configuration hadoopConf = createConfig();
    String user = hadoopClient.getKrbServiceProperty(Property.USER);

    return getConnection(user, hadoopConf, getConnectionString(database));
  }

  /**
   * Create new hive {@link Connection} object specified and unified database name.
   *
   * @param jwtToken oauth token
   * @param database database name
   * @return hive connection system for user that is identified by jwt token
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public Connection getConnection(JwtToken jwtToken, String database)
      throws LoginException, IOException, InterruptedException, URISyntaxException {
    Configuration hadoopConf = createConfig(jwtToken);
    String user = jwtToken.getUserId();

    return getConnection(user, hadoopConf, getConnectionString(database));
  }

  @VisibleForTesting
  static Connection getConnection(String user, Configuration hadoopConf, String jdbcUrl)
      throws InterruptedException, IOException {
    String ticketCachePath = hadoopConf.get("hadoop.security.kerberos.ticket.cache.path");
    UserGroupInformation signedOnUserSubject =
        UserGroupInformation.getBestUGI(ticketCachePath, user);
    return (Connection) signedOnUserSubject.doAs((PrivilegedExceptionAction<Object>) () -> {
      Class.forName(JDBC_DRIVER);
      return DriverManager.getConnection(jdbcUrl, null, null);
    });
  }

  /**
   * Create new {@link Configuration} object.
   *
   * Authenticates (if needed) using password from configuration.
   *
   * @return configuration
   * @throws LoginException
   * @throws IOException
   */
  public Configuration createConfig() throws LoginException, IOException {
    return hadoopClient.createConfig();
  }

  /**
   * Create new {@link Configuration} object.
   *
   * Authenticates using of JwtToken from oauth server.
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
