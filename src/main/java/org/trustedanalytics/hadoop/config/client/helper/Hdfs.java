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
import org.apache.hadoop.fs.FileSystem;
import org.trustedanalytics.hadoop.config.client.JwtToken;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

/**
 * Provides access to hdfs filesystem and configuration. Applicable to services of type
 * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HDFS_TYPE}.
 *
 * Usage:
 * 1) Use case with one configured service account.
 *
 * 1.1) How to get hdfs configuration (one hdfs service bound)?
 * Configuration hdfsConf = Hdfs.newInstance().createConfig();
 *
 * 1.2) How to get hdfs file system object (one hdfs service bound)?
 * FileSystem hdfsFs = Hdfs.newInstance().createFileSystem();
 *
 * 1.3) How to get hdfs configuration for given service instance name?
 * Configuration hdfsConf = Hdfs.newInstance("hdfs-instance").createConfig();
 *
 * 1.4) How to get file system object  for given service instance name?
 * FileSystem hdfsFs = Hdfs.newInstance("hdfs-instance").createFileSystem();
 *
 *
 * 2) Use case with user identity from oauth (not yet implemented).
 *
 * 2.1) How to get hdfs configuration (one hdfs service bound)?
 * JwtToken jwtToken;
 * ...
 * Configuration hdfsConf = Hdfs.newInstance().createConfig(jwtToken);
 *
 * 2.2) How to get hdfs file system object (one hdfs service bound)?
 * JwtToken jwtToken;
 * ...
 * FileSystem hdfsFs = Hdfs.newInstance().createFileSystem(jwtToken);
 *
 * 2.3) How to get hdfs configuration for given service instance name?
 * JwtToken jwtToken;
 * ...
 * Configuration hdfsConf = Hdfs.newInstance("hdfs-instance").createConfig(jwtToken);
 *
 * 2.4) How to get file system object for given service instance name?
 * JwtToken jwtToken;
 * ...
 * FileSystem hdfsFs = Hdfs.newInstance("hdfs-instance").createFileSystem(jwtToken);
 */
public final class Hdfs {

  private final HadoopClient hadoopClient;

  /**
   * Creates new instance of Hdfs client helper. Assume that the only one instance of
   * {@link org.trustedanalytics.hadoop.config.client.ServiceType#HDFS_TYPE} is bound.
   *
   * @return new instance of Hdfs helper
   * @throws IOException
   */
  public static Hdfs newInstance() throws IOException {
    return new Hdfs(HadoopClient.Builder.newInstance().withServiceType(ServiceType.HDFS_TYPE).build());
  }

  /**
   * Creates new instance of Hdfs client helper for hdfs service named instanceName.
   *
   * @param instanceName hdfs service instance name
   * @return new instance of Hdfs helper
   * @throws IOException
   */
  public static Hdfs newInstance(String instanceName) throws IOException {
    return new Hdfs(HadoopClient.Builder.newInstance().withServiceName(instanceName).build());
  }

  @VisibleForTesting
  static Hdfs newInstanceForTests(HadoopClient.Builder builder) throws IOException {
    return new Hdfs(builder.build());
  }

  private Hdfs(HadoopClient hadoopClient) throws IOException {
    this.hadoopClient = hadoopClient;
  }

  /**
   * Create new {@link FileSystem} object.
   *
   * @return hdfs file system for service user (creds from app configuration)
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public FileSystem createFileSystem() throws LoginException,
                                              IOException,
                                              InterruptedException,
                                              URISyntaxException {
    Configuration hadoopConf = createConfig();
    String user = hadoopClient.getKrbServiceProperty(Property.USER);
    URI hdfsUri = new URI(hadoopClient.getServiceProperty(Property.HDFS_URI));
    return FileSystem.get(hdfsUri, hadoopConf, user);
  }

  /**
   * Create new {@link FileSystem} object.
   *
   * @param jwtToken oauth token
   * @return hdfs file system for user identified by jwt token
   * @throws LoginException, IOException, InterruptedException, URISyntaxException
   */
  public FileSystem createFileSystem(JwtToken jwtToken) throws LoginException,
                                                               IOException,
                                                               InterruptedException,
                                                               URISyntaxException {
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