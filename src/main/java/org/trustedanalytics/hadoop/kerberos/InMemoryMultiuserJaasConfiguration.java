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
package org.trustedanalytics.hadoop.kerberos;

import com.google.common.collect.Maps;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

class InMemoryMultiuserJaasConfiguration extends Configuration {

  private final Map<String, AppConfigurationEntry[]> mappedConfigurations;
  private static InMemoryMultiuserJaasConfiguration instance;

  private InMemoryMultiuserJaasConfiguration(Map<String, AppConfigurationEntry[]> mappedConfigurations) {
    this.mappedConfigurations = mappedConfigurations;
  }

  public static InMemoryMultiuserJaasConfiguration getInstance() {
    if (null == instance) {
      instance = new InMemoryMultiuserJaasConfiguration(Maps.newConcurrentMap());
    }
    return instance;
  }

  @Override
  public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
    return mappedConfigurations.get(name);
  }

  public void append(String name, AppConfigurationEntry[] conf) {
    mappedConfigurations.put(name, conf);
  }
}
