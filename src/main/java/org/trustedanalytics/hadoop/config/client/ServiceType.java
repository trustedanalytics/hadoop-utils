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
 * Indicates location of configuration section for particular
 * service types.
 */
public enum ServiceType implements Path {

  /**
   * Location of all instances of HBASE type services.
   */
  HBASE_TYPE(ConfigPath.createPath()
                 .add(configNode -> Lists.newArrayList(
                     configNode.find(ConfigConstants.HBASE_SERVICE_TYPE_NAME)))
  ),

  /**
   * Location of all instances of YARN type services.
   */
  YARN_TYPE(ConfigPath.createPath()
                .add(configNode -> Lists.newArrayList(
                    configNode.find(ConfigConstants.YARN_SERVICE_TYPE_NAME)))
  ),

  /**
   * Location of all instances of HDFS type services.
   */
  HDFS_TYPE(ConfigPath.createPath()
                .add(configNode -> Lists.newArrayList(
                    configNode.find(ConfigConstants.HDFS_SERVICE_TYPE_NAME)))
  ),

  /**
   * Location of all instances of ZOOKEEPER type services.
   */
  ZOOKEEPER_TYPE(ConfigPath.createPath()
                .add(configNode -> Lists.newArrayList(
                    configNode.find(ConfigConstants.ZOOKEEPER_SERVICE_TYPE_NAME)))
  ),

  /**
   * Location of all user provided services configuration.
   */
  USER_PROVIDED(ConfigPath.createPath()
                .add(configNode -> Lists.newArrayList(
                    configNode.find(ConfigConstants.USER_PROVIDED)))
  ),

  /**
   * Location of all gearpump services configuration.
   */
  GEAR_PUMP_TYPE(ConfigPath.createPath()
                     .add(configNode -> Lists.newArrayList(
                         configNode.find(ConfigConstants.GEAR_PUMP_SERVICE_TYPE_NAME)))
  );

  private final ConfigPath configPath;

  ServiceType(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath() {
    return this.configPath;
  }
}
