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

import com.google.common.collect.Lists;

import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;
import org.trustedanalytics.hadoop.config.internal.Path;

/**
 * Representation of configuration properties.
 */
public enum Property implements Path {

  /**
   * URI to hdfs catalog exposed to use.
   */
  HDFS_URI(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(configNode.find(
                                               ConfigConstants.HDFS_URI)))
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
   *
   */
  ZOOKEPER_URI(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_CLUSTER)))
  ),

  /**
   * Zookeeper node exposed to use.
   */
 ZOOKEPER_ZNODE(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_ZNODE)))
  ),

  /**
   * Headless account name, use to authenticate in kerberos.
   */
 USER(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.USER_PROP_NAME)))
  ),

  /**
   * Headless account password, use to authenticate in kerberos.
   */
  PASSWORD(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(
                                               configNode
                                                   .find(ConfigConstants.PASSWORD_PROP_NAME)))

  ),

  /**
   * Hbase namespace exposed to use.
   */
  HBASE_NAMESPACE(ConfigPath.createPath().add(configNode ->
                                                  Lists.newArrayList(
                                                      configNode.find(ConfigConstants.HBASE_PROP_NAME_NAMESPACE)))
  );



  private ConfigPath configPath;

  Property(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath()  {
    return ConfigPath.createPath().append(this.configPath);
  }

}
