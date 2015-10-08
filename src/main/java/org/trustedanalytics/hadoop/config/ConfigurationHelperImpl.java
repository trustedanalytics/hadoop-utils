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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.internal.Path;
import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigNode;
import org.trustedanalytics.hadoop.config.internal.ConfigurationReader;
import org.trustedanalytics.hadoop.config.internal.JsonConfigurationReader;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Deprecated
public final class ConfigurationHelperImpl implements ConfigurationHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHelperImpl.class);

  private FactoryHelper factoryHelper;

  ConfigurationHelperImpl(FactoryHelper factoryHelper) {
    this.factoryHelper = factoryHelper;
  }

  /**
   * Creates  new instance of helper.
   *
   * @return
   */
  public static ConfigurationHelperImpl getInstance() {
    return new ConfigurationHelperImpl(new FactoryHelper());
  }

  @Override
  public Map<String, String> getConfigurationFromJson(String jsonConf, ConfigurationLocator location)
      throws IOException {
    List<ConfigNode> found = pickConfNodes(jsonConf, location);
    Map<String, String> config = new HashMap<>();
    found.forEach(confSection ->
                      confSection.getChildren().forEach(param ->
                                                             config.put(param.name(), param.value())
                      )
    );

    LOGGER.info("Returning following config to app: " + config);
    return config;
  }

  @Override
  public Map<String, String> getConfigurationFromEnv(String envVariableName, ConfigurationLocator location)
      throws IOException {
    return getConfigurationFromJson(System.getenv(envVariableName), location);
  }

  @Override
  public Map<String, String> getConfigurationFromEnv(ConfigurationLocator location) throws IOException {
    return getConfigurationFromEnv(ConfigConstants.VCAP_SERVICES, location);
  }

  @Override
  public Optional<String> getPropertyFromJson(String jsonConf, PropertyLocator location)
      throws IOException {
    String  value = null;
    if(jsonConf != null) {
      List<ConfigNode> found = pickConfNodes(jsonConf, location);
      for (ConfigNode node : found) {
        value = node.value();
      }
    }
    return Optional.ofNullable(value);
  }

  @Override
  public Optional<String> getPropertyFromEnv(String envVariableName, PropertyLocator location)
      throws IOException {
    return getPropertyFromJson(System.getenv(envVariableName), location);
  }

  @Override
  public Optional<String> getPropertyFromEnv(PropertyLocator location) throws IOException {
    return getPropertyFromEnv(ConfigConstants.VCAP_SERVICES, location);
  }

  static final class FactoryHelper {
    ConfigurationReader getConfigurationReader(String jsonConf) throws IOException {
      return JsonConfigurationReader.getReader(jsonConf);
    }
  }

  private List<ConfigNode> pickConfNodes(String jsonConf, Path location) throws IOException {
    ConfigurationReader reader = factoryHelper.getConfigurationReader(jsonConf);
    ConfigNode node = reader.getRootNode();
    List<ConfigNode> found = new ArrayList<>();
    for (Function<ConfigNode, List<ConfigNode>> action : location.getConfPath().getStack()) {
      found = action.apply(node);
      if (found.size() == 1) {
        node = found.get(0);
      } else {
        break;
      }
    }
    return found;
  }

}
