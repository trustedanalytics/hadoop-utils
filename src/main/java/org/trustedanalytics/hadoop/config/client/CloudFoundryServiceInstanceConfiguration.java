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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.internal.ConfigConstants;
import org.trustedanalytics.hadoop.config.internal.ConfigNode;
import org.trustedanalytics.hadoop.config.internal.ConfigPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class CloudFoundryServiceInstanceConfiguration implements ServiceInstanceConfiguration {

  private ConfigNode configuration;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CloudFoundryServiceInstanceConfiguration.class);

  private CloudFoundryServiceInstanceConfiguration(ConfigNode conf) {
    this.configuration = conf;
  }

  static List<ServiceInstanceConfiguration> getListConfiguration(ConfigNode root, ConfigPath path) {
    ConfigNode node = root;
    List<ServiceInstanceConfiguration> ret = new ArrayList<>();
    int step = 1;
    int pathLength = path.getStack().size();
    //path interpretation logic
    for (Function<ConfigNode, List<ConfigNode>> action : path.getStack()) {
      List<ConfigNode> found = action.apply(node);
      if (found.size() == 1) {
        //do next step on path
        node = found.get(0);
      } else if (found.size() > 1 && step == pathLength) {
        //on the end of path we expect list
        found.forEach(config -> ret.add(new CloudFoundryServiceInstanceConfiguration(config)));
      } else {
        //fork is illegal in the middle of path
        throw new IllegalStateException("Incorrect configuration path! Path can't fork!");
      }
      step++;
    }
    return ret;
  }

  static Optional<ServiceInstanceConfiguration> getConfiguration(ConfigNode root, ConfigPath path) {
    ConfigNode node = root;
    ServiceInstanceConfiguration ret = null;
    try {
      //path interpretation logic
      for (Function<ConfigNode, List<ConfigNode>> action : path.getStack()) {
        List<ConfigNode> found = action.apply(node);
        if (found.size() != 1) {
          throw new IllegalStateException("Ambiguity. Impossible to determine which configuration "
                                          + "should be returned!");
        }
        node = found.get(0);
      }
      ret = new CloudFoundryServiceInstanceConfiguration(node);
    } catch (NullPointerException ignore) {
      LOGGER.info("Attempting to read not existing configuration! " + ignore.getMessage());
    }
    return Optional.ofNullable(ret);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return configuration.name();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Configuration asHadoopConfiguration() {
    Configuration conf = new Configuration(false);
    configuration.find(ConfigConstants.HADOOP_CONFIG_KEY_VALUE).getChildren().forEach(
        param -> conf.set(param.name(), param.value())
    );
    return conf;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> asMap() {
    Map<String, String> properties = new HashMap<>();
    configuration.find(ConfigConstants.HADOOP_CONFIG_KEY_VALUE).getChildren().forEach(
        param -> properties.put(param.name(), param.value())
    );
    return properties;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getProperty(Property propertyLocation)
      throws IllegalStateException {
    ConfigNode node = this.configuration;
    try {
      for (Function<ConfigNode, List<ConfigNode>> action : propertyLocation.getConfPath()
          .getStack()) {
        List<ConfigNode> found = action.apply(node);
        if (found.size() != 1) {
          throw new IllegalStateException("Incorrect property path! Path can't fork!");
        }
        node = found.get(0);
      }
    } catch (NullPointerException ignore) {
      LOGGER.debug("Not existing property!", ignore);
      return Optional.empty();
    }
    return Optional.ofNullable(node.value());
  }

}
