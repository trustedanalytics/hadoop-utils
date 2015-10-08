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

import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.JsonConfigurationReader;

import java.io.IOException;

/**
 * Factory for application configuration helper object.
 */
public class Configurations {

  private Configurations() {}

  /**
   * Creates new instance of application configuration. Configuration parameters
   * are read from default environment variable {@link ConfigConstants#VCAP_SERVICES}.
   *
   * @return new instance of application config
   */
  public static AppConfiguration newInstanceFromEnv() throws IOException {
    return newInstanceFromJson(System.getenv(ConfigConstants.VCAP_SERVICES));
  }

  /**
   * Creates new instance of application configuration. Configuration has to be supplied in JSON
   * string.
   *
   * @param  conf app config serialized to string
   * @return new instance of application config
   */
  public static AppConfiguration newInstanceFromJson(String conf) throws IOException {
    return new CloudFoundryAppConfiguration(JsonConfigurationReader.getReader(conf).getRootNode());
  }
}
