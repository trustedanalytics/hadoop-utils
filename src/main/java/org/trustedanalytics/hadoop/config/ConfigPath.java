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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfigPath {

  List<Function<ConfigNode, List<ConfigNode>>> stack = new ArrayList<>();

  static ConfigPath path;

  static ConfigPath createPath() {
    path = new ConfigPath();
    return path;
  }

  ConfigPath add(Function<ConfigNode, List<ConfigNode>> action) {
    stack.add(action);
    return path;
  }

  List<Function<ConfigNode, List<ConfigNode>>> getStack() {
    return this.stack;
  }

}
