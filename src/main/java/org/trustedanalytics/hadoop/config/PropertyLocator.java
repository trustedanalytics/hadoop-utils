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

public enum PropertyLocator implements Track {

  HDFS_URI(ConfigPath.createPath().add(configNode ->
                                           Lists.newArrayList(
                                               configNode
                                                   .find(ConfigConstants.HDFS_SERVICE_TYPE_NAME)
                                                   .find(ConfigConstants.HDFS_URI)))
  ),

  KRB_KDC(ConfigPath.createPath().add(configNode ->
                                          Lists.newArrayList(
                                              configNode
                                                  .find(ConfigConstants.KRB_CONF_NODE_NAME)
                                                  .get(ConfigConstants.KDC_ADRESS_PROP_NAME)))
  ),

  KRB_REALM(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                    .find(ConfigConstants.KRB_CONF_NODE_NAME)
                                                    .get(ConfigConstants.REALM_NAME_PROP_NAME)))
  ),

  ZOOKEPER_URI(ConfigPath.createPath().add(configNode ->
                                            Lists.newArrayList(
                                                configNode
                                                        .get(ConfigConstants.ZOOKEEPER_SERVICE_TYPE_NAME)
                                                        .find(ConfigConstants.CREDENTIALS)
                                                        .get(ConfigConstants.ZOOKEEPER_ClUSTER)))
  );

  private ConfigPath configPath;

  PropertyLocator(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath() {
    return this.configPath;
  }

}
