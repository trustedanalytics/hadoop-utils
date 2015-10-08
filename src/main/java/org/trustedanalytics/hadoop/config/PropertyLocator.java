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
package org.trustedanalytics.hadoop.config;

import com.google.common.collect.Lists;

import org.trustedanalytics.hadoop.config.internal.Path;
import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;

@Deprecated
public enum PropertyLocator implements Path {

  HDFS_URI(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(configNode.find(
                                               ConfigConstants.HDFS_URI)))
  ),

  KRB_KDC(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.KDC_ADRESS_PROP_NAME)))
  ),

  KRB_REALM(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.REALM_NAME_PROP_NAME)))
  ),


  ZOOKEPER_URI(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_CLUSTER)))
  ),

 ZOOKEPER_ZNODE(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode.find(ConfigConstants.ZOOKEEPER_ZNODE)))
  ),

 USER(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.USER_PROP_NAME)))
  ),

  PASSWORD(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(
                                               configNode
                                                   .find(ConfigConstants.PASSWORD_PROP_NAME)))

  ),

  HBASE_NAMESPACE(ConfigPath.createPath().add(configNode ->
                                                  Lists.newArrayList(
                                                      configNode.find(ConfigConstants.HBASE_PROP_NAME_NAMESPACE)))
  );



  private ConfigPath configPath;

  PropertyLocator(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath()  {
    return ConfigPath.createPath().append(this.configPath);
  }

}
