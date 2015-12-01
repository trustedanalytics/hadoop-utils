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
 * Represents configuration for hadoop service instances
 * (HDFS, YARN, HBASE, GEARPUMP, ZOOKEEPER, ... ).
 */
public interface ServiceInstanceConfiguration {

  /**
   * Returns that service instance name.
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
   * Returns configuration properties for that service instance.
   *
   * @return collection of configuration properties.
   */
  Map<String, String> asMap();

  /**
   * Returns property from configuration. Property name has to be unique in the whole
   * configuration otherwise {@link IllegalStateException} will be thrown.
   *
   * @param propertyLocation property location
   * @return optional property value
   * @throws IllegalStateException
   */
  Optional<String> getProperty(Property propertyLocation) throws IllegalStateException;
}
