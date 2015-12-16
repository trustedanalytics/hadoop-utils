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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationsTest {

  private String jsonSerializedEnv;

  @Before
  public void setUp() throws Exception {
    jsonSerializedEnv = IOUtils.toString(getClass().getResourceAsStream("/env_vcap_service.json"));
  }

  @Test
  public void testNewInstanceFromJson_correctConfSerializedToJson_returnsAppConfigurationObject()
      throws Exception {
    AppConfiguration configuration = Configurations.newInstanceFromJson(jsonSerializedEnv);

    List<ServiceInstanceConfiguration> hbases =
        configuration.getServiceConfigList(ServiceType.HBASE_TYPE);
    ServiceInstanceConfiguration hdfs = configuration.getServiceConfig(ServiceType.HDFS_TYPE);
    ServiceInstanceConfiguration yarn = configuration.getServiceConfig(ServiceType.YARN_TYPE);

    assertThat(hbases, hasSize(2));
    assertThat(hdfs.getName(), is("hdfs-instance"));
    assertThat(yarn.getName(), is("yarn-instance"));
  }
}