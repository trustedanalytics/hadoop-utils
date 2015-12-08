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
package org.trustedanalytics.hadoop.config.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;

public final class JsonConfigurationReader
    implements ConfigurationReader {

  private ConfigNode rootNode;

  public static JsonConfigurationReader getReader(String json) throws IOException {
    Preconditions.checkNotNull(json);
    return new JsonConfigurationReader(json);
  }

  static final class FactoryHelper {
    ObjectMapper getJsonMapper() {
      return new ObjectMapper();
    }
  }

  JsonConfigurationReader(FactoryHelper helperFactory, String json) throws IOException {
    rootNode = JsonConfigNode.createInstance(helperFactory.getJsonMapper().readTree(json));
  }

  private JsonConfigurationReader(String json) throws IOException {
    this(new FactoryHelper(), json);
  }

  @Override
  public ConfigNode getRootNode() {
    return rootNode;
  }
}