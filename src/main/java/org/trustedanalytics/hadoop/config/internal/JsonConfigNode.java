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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JsonConfigNode implements ConfigNode {

  private JsonNode root;

  private String name = null;

  private JsonConfigNode(String name, JsonNode rootNode) {
    this.root = rootNode;
    this.name = name;
  }

  private JsonConfigNode(JsonNode rootNode) {
    this.root = rootNode;
  }

  public static JsonConfigNode createInstance(JsonNode node) {
    return new JsonConfigNode(node);
  }

  public static JsonConfigNode createInstance(String name, JsonNode node) {
    return new JsonConfigNode(name, node);
  }

  @Override
  public List<ConfigNode> getChildren() {
    List<ConfigNode> ret = new ArrayList<>();
    if (root.isArray()) {
      root.elements().forEachRemaining(child -> {
        JsonNode nameNode = child.get(ConfigConstants.INSTANCE_NAME_PROP_NAME);
        if (nameNode == null) {
          ret.add(createInstance(child));
        } else {
          ret.add(createInstance(nameNode.asText(), child));
        }
      });
    } else {
      root.fields().forEachRemaining(child -> {
        String localName = child.getKey();
        JsonNode value = child.getValue();
        ret.add(createInstance(localName, value));
      });
    }
    return ret;
  }

  @Override
  public ConfigNode find(String name) {
    JsonNode found = Preconditions.checkNotNull(root.findValue(name),
                                                "Can not find config node: " + name);
    return createInstance(name, found);
  }

  @Override
  public List<ConfigNode> findAll(String name) {
    List<JsonNode> parents =
        Preconditions.checkNotNull(root.findParents(name),
                                   "None of " + name + " nodes found in configuration");
    List<ConfigNode> found = new ArrayList<>();
    parents.forEach(parent -> found.add(JsonConfigNode.createInstance(name, parent.get(name))));
    return found;
  }

  @Override
  public ConfigNode get(String name) {
    JsonNode got = Preconditions.checkNotNull(root.get(name),
                                              "Can not get config node: " + name);
    return createInstance(name, got);
  }

  @Override
  public ConfigNode selectOne(String name, String value) {
    List<JsonNode> parents =
        Preconditions.checkNotNull(root.findParents(name),
                                   "None of " + name + " nodes found in configuration");
    //We assume that only one child node meets these conditions.
    Predicate<JsonNode> conditions =
        parent -> parent.get(name).isTextual() && parent.get(name).textValue().equals(value);
    Optional<JsonNode> found = parents.stream().filter(conditions).findFirst();
    return createInstance(value,
        found.orElseThrow(() -> new NullPointerException("Can't find child node that has name "
                                                         + name + " and value " + value + "!")
        ));
  }

  @Override
  public String value() {
    return Preconditions.checkNotNull(root.asText());
  }

  @Override
  public String name() {
    Preconditions.checkNotNull(this.name);
    return this.name;
  }
}
