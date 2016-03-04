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

import java.util.List;
import java.util.Optional;

/**
 * Current use cases :
 * 1. App is using one instance of service (selected by type {@link ServiceType})
 * 2. App is using many instances of service of the same type.
 *    For instance : two hbase instances, src and dst, (selected by instance name)
 * 3. App can ask for all instances of the given type.
 */
public interface AppConfiguration {

  /**
   * Returns Cloud Foundry service configuration for service instance named serviceInstanceName.
   *
   * @param serviceInstanceName service instance name
   * @return service configuration
   */
  ServiceInstanceConfiguration getServiceConfig(String serviceInstanceName);

  /**
   * Returns Cloud Foundry service configuration for service instance with given type.
   *
   * @param serviceTypeLocation config section location for service type
   * @return service configuration
   * @throws IllegalStateException when it is impossible to find service instance of a given type
   * (for example: No instance of a given type was bound or more instances of the same type were
   * bound).
   */
  ServiceInstanceConfiguration getServiceConfig(ServiceType serviceTypeLocation)
      throws IllegalStateException;

  /**
   * Returns list of Cloud Foundry services configurations for all service instances of a specified type.
   *
   * @param serviceTypeLocation config section location
   * @return configs for all service instances of a specified type
   */
  List<ServiceInstanceConfiguration> getServiceConfigList(ServiceType serviceTypeLocation);

  /**
   * Returns Cloud Foundry optional service configuration for service instance with given type.
   *
   * @param serviceTypeLocation config section location for service type
   * @return optional service configuration
   */
  Optional<ServiceInstanceConfiguration> getServiceConfigIfExists(ServiceType serviceType);
}
