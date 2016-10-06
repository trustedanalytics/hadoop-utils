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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleAppConfiguration implements AppConfiguration {

  private final Map<ServiceType, ServiceInstanceConfiguration> svcConfigurations;
  
  public SimpleAppConfiguration(Map<ServiceType, ServiceInstanceConfiguration> svcConfigurations) {
    this.svcConfigurations = svcConfigurations;
  }
  
  @Override
  public ServiceInstanceConfiguration getServiceConfig(String serviceInstanceName) {
    return svcConfigurations.values().stream()
        .filter(c -> c.getName().equals(serviceInstanceName))
        .findFirst()
        .get();
  }

  @Override
  public ServiceInstanceConfiguration getServiceConfig(ServiceType serviceTypeLocation)
      throws IllegalStateException {
    return svcConfigurations.get(serviceTypeLocation);
  }

  @Override
  public List<ServiceInstanceConfiguration> getServiceConfigList(ServiceType serviceTypeLocation) {
    // This implementation supports only one configuration per service type
    return Collections.singletonList(svcConfigurations.get(serviceTypeLocation));
  }

  @Override
  public Optional<ServiceInstanceConfiguration> getServiceConfigIfExists(ServiceType serviceType) {
    return Optional.ofNullable(svcConfigurations.get(serviceType));
  }

}
