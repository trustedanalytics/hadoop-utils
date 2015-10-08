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

/**
 * Current use cases :
 * 1. App is using one instance of service, (choosing by type {@link ServiceType})
 * 2. App is using many instance of service of the same type.
 *    For instance two hbase instances, src and dst, (choosing by instance name)
 * 3. App will ask for all instances of the given type.
 */
public interface AppConfiguration {

  /**
   * Return cloud foundry service configuration for service instance named serviceInstanceName.
   *
   * @param serviceInstanceName service instance name
   * @return service configuration
   */
  ServiceInstanceConfiguration getServiceConfig(String serviceInstanceName);

  /**
   * Return cloud foundry service configuration for service in{}stance given type.
   *
   * @param serviceTypeLocation config section localization for service type
   * @return service configuration
   * @throws IllegalStateException throws when is impossible to find/determine service instance
   * which configuration has to be returned
   * (for example: No instance of given type was bound. More then one instances the same type was
   * bound)
   */
  ServiceInstanceConfiguration getServiceConfig(ServiceType serviceTypeLocation)
      throws IllegalStateException;

  /**
   * Return list of cloud foundry service configurations for all service instances given type.
   *
   * @param serviceTypeLocation config section localization for service type
   * @return configs for all service instances specified type
   */
  List<ServiceInstanceConfiguration> getServiceConfigList(ServiceType serviceTypeLocation);

}
