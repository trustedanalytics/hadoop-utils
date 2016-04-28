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
package org.trustedanalytics.hadoop.config.client;

import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;
import org.trustedanalytics.hadoop.config.internal.Path;

import com.google.common.collect.Lists;

/**
 * Configuration properties.
 */
public enum Property implements Path {

  /**
   * Hdfs directory URI used by broker.
   */
  HDFS_URI(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(configNode.find(
                                               ConfigConstants.HDFS_URI)))
  ),

  /**
   * Hive directory URI used by broker.
   */
  HIVE_URL(ConfigPath.createPath().add(configNode ->
      Lists.newArrayList(configNode.find(
          ConfigConstants.HIVE_URL)))
  ),


  /**
   * Kerberos KDC address.
   */
  KRB_KDC(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.KDC_ADRESS_PROP_NAME)))
  ),

  /**
   * Default kerberos realm.
   */
  KRB_REALM(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.REALM_NAME_PROP_NAME)))
  ),

  /**
   * Base64 from cacert.
   */
  KRB_CACERT(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.KRB_CACERT_PROP_NAME)))
  ),

  /**
   * Detrmine is kerberos enabled.
   */
  KRB_MODE(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.KRB_MODE_PROP_NAME)))
  ),

  /**
   *  Zookeeper cluster address.
   */
  ZOOKEPER_URI(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_CLUSTER)))
  ),

  /**
   * Zookeeper node used by broker.
   */
  ZOOKEPER_ZNODE(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_ZNODE)))
  ),

  /**
   * Headless account name, used for authentication in Kerberos.
   */
  USER(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.USER_PROP_NAME)))
  ),

  /**
   * Headless account password, used for authentication in Kerberos.
   */
  PASSWORD(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(
                                               configNode
                                                   .find(ConfigConstants.PASSWORD_PROP_NAME)))

  ),

  /**
   * Hbase namespace used by broker.
   */
  HBASE_NAMESPACE(ConfigPath.createPath().add(configNode ->
                                                  Lists.newArrayList(
                                                      configNode.find(ConfigConstants.HBASE_PROP_NAME_NAMESPACE)))
  ),

  /**
   * Yarn queue name.
   */
  YARN_QUEUE(ConfigPath.createPath().add(configNode ->
                                                  Lists.newArrayList(
                                                      configNode.find(ConfigConstants.YARN_QUEUE)))
  ),

  /**
   * Base64 from configuration zip.
   */
  HADOOP_ZIP(ConfigPath.createPath().add(configNode ->
                                        Lists.newArrayList(
                                            configNode.find(ConfigConstants.HADOOP_CONFIG_ZIP_VALUE).find(ConfigConstants.ZIP)))
  );


  private final transient ConfigPath configPath;

  Property(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath()  {
    return ConfigPath.createPath().append(this.configPath);
  }

}
