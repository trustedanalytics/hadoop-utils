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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CloudFoundryAppConfigurationTest {

  private static final String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  private static String vcapServices;

  @Before
  public void setUp() {
    try(InputStream hl = getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH)) {
      vcapServices = IOUtils.toString(hl);
    } catch (IOException ignored) {
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testGetServiceConfig_notExistentServiceInstance_throwsExecption() throws Exception {
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);
    helper.getServiceConfig("not-existent-instance");
  }

  @Test
  public void testGetServiceConfig_existingServiceInstance_returnConfiguration() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    ServiceInstanceConfiguration configuration = helper.getServiceConfig("hbase-instance1");
    Map<String, String> actual = configuration.asMap();

    //then
    Map<String, String> expected = new HashMap<>();
    expected.put("hbase.zookeeper.quorum",
                 "cdh-master-0.node.gotapaaseu.consul,cdh-master-1.node.gotapaaseu.consul,cdh-master-2.node.gotapaaseu.consul");
    assertThat(actual, equalTo(expected));
  }

  @Test
  public void testGetServiceConfigList_twoServiceInstancesBound_returnConfigsList()
      throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    List<ServiceInstanceConfiguration> actual = helper.getServiceConfigList(ServiceType.HBASE_TYPE);

    //then
    assertThat(actual.size(), equalTo(2));
    assertThat(actual.get(0).getName(), equalTo("hbase-instance1"));
    assertThat(actual.get(1).getName(), equalTo("hbase-instance2"));

    Map<String, String> expected = new HashMap<>();
    expected.put("hbase.zookeeper.quorum",
                 "cdh-master-0.node.gotapaaseu.consul,cdh-master-1.node.gotapaaseu.consul,cdh-master-2.node.gotapaaseu.consul");
    assertThat(actual.get(0).asMap(), equalTo(expected));
    expected.clear();
    expected.put("hbase.zookeeper.quorum",
                 "cdh-master-0,cdh-master-1,cdh-master-2");
    expected.put("hadoop.security.authentication", "kerberos");
    assertThat(actual.get(1).asMap(), equalTo(expected));
  }

  @Test
  public void getServiceConfig_oneServiceConfig_returnConfiguration() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    ServiceInstanceConfiguration actual = helper.getServiceConfig(ServiceType.HDFS_TYPE);

    //then
    assertThat(actual.asMap().size(), equalTo(2));
    Map<String, String> expected = new HashMap<>();
    expected.put("hadoop.security.authentication", "kerberos");
    expected.put("dfs.namenode.kerberos.principal", "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL");
    assertThat(actual.asMap(), equalTo(expected));
  }

  @Test(expected = IllegalStateException.class)
  public void getServiceConfig_notExistentService_throwsException() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    helper.getServiceConfig(ServiceType.GEAR_PUMP_TYPE);
    //then
    //throws exception
  }

  @Test(expected = IllegalStateException.class)
  public void getServiceConfig_oneServiceConfigExpectedFoundTwo_throwException() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    helper.getServiceConfig(ServiceType.HBASE_TYPE);

    //then
    //throws exception
  }

  @Test
  public void testGetProperty_getExistingProperty_returnUriProperty() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig(ServiceType.HDFS_TYPE).getProperty(Property.HDFS_URI);

    //then
    assertThat(actual.get(),
               equalTo(
                   "hdfs://localhost/cf/intel/instances/1cfe7b45-1e07-4751-a853-78ef47a313cc/"));
  }

  @Test
  public void testGetProperty_getNotExistingProperty() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig(ServiceType.HDFS_TYPE).getProperty(Property.HBASE_NAMESPACE);
    //then
    assertThat(actual.isPresent(), equalTo(false));
  }

  @Test
  public void getProperty_concreteServiceInstanceProperty1_returnValue() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig("hbase-instance1").getProperty(Property.HBASE_NAMESPACE);

    //then
    assertThat(actual.get(), equalTo("6f1bb0fdab0502079c4c4ca6bc770574fe546fc1"));
  }

  @Test
  public void getProperty_returnValue() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig(ServiceType.ZOOKEEPER_TYPE).getProperty(Property.ZOOKEPER_URI);

    //then
    assertThat(actual.get(), equalTo("0.0.0.0,1.1.1.1"));
  }

  @Test
  public void getProperty_upsKerberosService_returnKDCValue() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig("kerberos-service").getProperty(Property.KRB_KDC);

    //then
    assertThat(actual.get(), equalTo("ip-10-10-9-198.us-west-2.compute.internal"));
  }

  @Test
  public void getProperty_upsKerberosService_returnRealmValue() throws Exception {
    //given
    AppConfiguration helper = Configurations.newInstanceFromJson(vcapServices);

    //when
    Optional<String> actual =
        helper.getServiceConfig("kerberos-service").getProperty(Property.KRB_REALM);

    //then
    assertThat(actual.get(), equalTo("US-WEST-2.COMPUTE.INTERNAL"));
  }

}
