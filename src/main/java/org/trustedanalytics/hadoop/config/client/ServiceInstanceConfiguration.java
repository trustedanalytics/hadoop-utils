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

import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.hadoop.config.client.Property;

import java.util.Map;
import java.util.Optional;

/**
 * Represents configuration of some hadoop service instance.
 * HDFS, YARN, HBASE, GEARPUMP, ZOOKEEPER, ... .
 */
public interface ServiceInstanceConfiguration {

  /**
   * Return service instance name.
   *
   * @return service instance name
   */
  String getName();

  /**
   * Create new instance of {@link Configuration}.
   *
   * @return hadoop client configuration
   */
  Configuration asHadoopConfiguration();

  /**
   * Return collection of all configuration parameters set for service instance.
   *
   * @return collection of all configuration parameters
   */
  Map<String, String> asMap();

  /**
   * Return single parameter from configuration. Parameter name has to bee unique in whole
   * configuration otherwise it throws {@link IllegalStateException}.
   *
   * @param propertyLocation parameter localization
   * @return optional value of parameter
   * @throws IllegalStateException
   */
  Optional<String> getProperty(Property propertyLocation) throws IllegalStateException;
}
