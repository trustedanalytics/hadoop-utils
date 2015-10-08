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
import org.trustedanalytics.hadoop.config.internal.ConfigNode;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;

import java.util.List;
import java.util.Optional;

public final class CloudFoundryAppConfiguration implements AppConfiguration {

  private ConfigNode rootConfigNode;

  CloudFoundryAppConfiguration(ConfigNode node) {
    this.rootConfigNode = node;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServiceInstanceConfiguration getServiceConfig(String serviceInstanceName) {
    Optional<ServiceInstanceConfiguration> found = Optional.empty();
    // search configuration of all predefined types
    for(ServiceType serviceType : ServiceType.values()) {
      ConfigPath configPath = ConfigPath .createPath().append(serviceType.getConfPath()).add(
          configNode -> Lists.newArrayList(configNode
                                               .selectOne(ConfigConstants.INSTANCE_NAME_PROP_NAME,
                                                          serviceInstanceName)));
      found =
          CloudFoundryServiceInstanceConfiguration.getConfiguration(this.rootConfigNode, configPath);
      if (found.isPresent()) {
        break;
      }
    }
    return found.orElseThrow(() ->
                 new IllegalStateException("Not found configuration for service instance named "
                                            + serviceInstanceName + "!"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServiceInstanceConfiguration getServiceConfig(ServiceType serviceTypeLocation)
      throws IllegalStateException {
    ConfigPath configPath = ConfigPath.createPath().append(serviceTypeLocation.getConfPath())
        .add(ConfigNode::getChildren);
    Optional<ServiceInstanceConfiguration> found =
        CloudFoundryServiceInstanceConfiguration.getConfiguration(this.rootConfigNode,
                                                                  configPath);
    return found.orElseThrow(() -> new IllegalStateException("Not found "
                                                             + serviceTypeLocation
                                                             + " service configuration!"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ServiceInstanceConfiguration> getServiceConfigList(ServiceType serviceTypeLocation) {

    ConfigPath typeSection = ConfigPath.createPath().append(serviceTypeLocation.getConfPath())
                             .add(ConfigNode::getChildren);

    return CloudFoundryServiceInstanceConfiguration.getListConfiguration(this.rootConfigNode,
                                                                         typeSection);
  }

}
