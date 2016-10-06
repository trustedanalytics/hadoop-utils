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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.trustedanalytics.hadoop.config.client.ServiceType.HIVE_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SimpleAppConfigurationTest {

  private AppConfiguration appConfig;
  private ServiceInstanceConfiguration testHiveConfig;

  @Before
  public void before() {
    testHiveConfig = new SimpleInstanceConfiguration("test", null, Collections.emptyMap());
    Map<ServiceType, ServiceInstanceConfiguration> svcMap = new HashMap<>(); 
    svcMap.put(HIVE_TYPE, testHiveConfig);
    appConfig = new SimpleAppConfiguration(svcMap);
  }
  
  @Test
  public void getServiceConfigByName() {
    assertThat(appConfig.getServiceConfig("test"), equalTo(testHiveConfig));
  }
  
  @Test
  public void getServiceConfigByType() {
    assertThat(appConfig.getServiceConfig(HIVE_TYPE), equalTo(testHiveConfig));
  }
  
  @Test
  public void getServiceConfigList() {
    assertThat(appConfig.getServiceConfigList(HIVE_TYPE).iterator().next(), equalTo(testHiveConfig));
  }
  
  @Test
  public void getServiceConfigIfExists() {
    assertThat(appConfig.getServiceConfigIfExists(HIVE_TYPE).get(), equalTo(testHiveConfig));
  }
}
