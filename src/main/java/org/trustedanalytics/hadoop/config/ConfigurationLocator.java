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

import org.trustedanalytics.hadoop.config.internal.Path;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;

@Deprecated
public enum ConfigurationLocator implements Path {

  HADOOP(ConfigPath.createPath()
             .add(configNode -> configNode.findAll(ConfigConstants.HADOOP_CONFIG_KEY_VALUE))
  ),

  YARN(ConfigPath.createPath().append(ServiceType.YARN_TYPE.getConfPath())
           .add(configNode -> configNode.findAll(ConfigConstants.HADOOP_CONFIG_KEY_VALUE))

  ),

  HDFS(ConfigPath.createPath().append(ServiceType.HDFS_TYPE.getConfPath())
           .add(configNode -> configNode.findAll(ConfigConstants.HADOOP_CONFIG_KEY_VALUE))
  ),

  HBASE(ConfigPath.createPath().append(ServiceType.HBASE_TYPE.getConfPath())
                .add(configNode -> configNode.findAll(ConfigConstants.HADOOP_CONFIG_KEY_VALUE))
  );


  private ConfigPath configPath;

  ConfigurationLocator(ConfigPath path) {
    this.configPath = path;
  }

  @Override
  public ConfigPath getConfPath() {
    return ConfigPath.createPath().append(this.configPath);
  }
}
