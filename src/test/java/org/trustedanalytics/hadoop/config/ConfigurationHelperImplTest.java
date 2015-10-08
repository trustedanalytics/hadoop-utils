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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

public class ConfigurationHelperImplTest {

  private static String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  private static String vcapServices;

  @Before
  public void setUp() {
    try(InputStream hl = getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH)) {
      vcapServices = IOUtils.toString(hl);
    } catch (IOException ignored) {
    }
  }

  @Test
  public void testGetConfFromJson_correctConfJSON_returnAllConfProps()
      throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams =
        helper.getConfigurationFromJson(vcapServices, ConfigurationLocator.HADOOP);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(4)));
    Assert.assertThat(configParams, allOf(hasEntry("yarn.resourcemanager.hostname",
                                                   "0.0.0.0"),
                                          hasEntry("hadoop.security.authentication", "kerberos"),
                                          hasEntry("dfs.namenode.kerberos.principal",
                                                   "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test
  public void testGetConfFromJson_correctEmptyConfJSON_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_SERVICES\":{}}";
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> actual = helper.getConfigurationFromJson(jsonConf,
                                                                 ConfigurationLocator.HADOOP);

    Map<String, String> expected = new HashMap<>();

    Assert.assertThat(actual, equalTo(expected));
  }

  @Test
  public void testGetConfFromJson_correctJSONNoVcapServicesConf_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_APPLICATION\":{}}";
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> actual =
        helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.HADOOP);

    Map<String, String> expected = new HashMap<>();

    Assert.assertThat(actual, equalTo(expected));
  }

  @Test(expected = IOException.class)
  public void testGetHadoopConfFromJson_incorrectJSON_throwsException() throws Exception {
    String incorrectJsonConf = "{\"VCAP_APPLICATION\":{}";
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    helper.getConfigurationFromJson(incorrectJsonConf, ConfigurationLocator.HADOOP);
  }

  @Test(expected = NullPointerException.class)
  public void testGetConfFromJson_nullJsonString_throwsException() throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    helper.getConfigurationFromJson(null, ConfigurationLocator.HADOOP);
  }

  @Test(expected = NullPointerException.class)
  public void testGetConfFromEnv_notSetEnvironmentVariable_throwsException() throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    helper.getConfigurationFromEnv("not_set_env_variable_name", ConfigurationLocator.HADOOP);
  }

  @Test
  public void testGetConfByServiceName_correctConfJSONAndServiceName_returnHDFSServiceConfProps()
      throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams = helper.getConfigurationFromJson(vcapServices,
                                                                       ConfigurationLocator.HDFS);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(2)));
    Assert.assertThat(configParams, allOf(hasEntry("hadoop.security.authentication", "kerberos"),
                                          hasEntry("dfs.namenode.kerberos.principal",
                                                   "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test
  public void testGetConfByServiceName_correctConfJSONAndServiceName_returnYARNServiceConfProps()
      throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams =
        helper.getConfigurationFromJson(vcapServices, ConfigurationLocator.YARN);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(1)));
    Assert.assertThat(configParams, allOf(hasEntry("yarn.resourcemanager.hostname", "0.0.0.0")));
  }

  @Test
  public void testGetConfByServiceName_correctConfJSONAndServiceName_returnHBASEServiceConfProps()
      throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams =
        helper.getConfigurationFromJson(vcapServices, ConfigurationLocator.HBASE);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(2)));
    Assert.assertThat(configParams, allOf(hasEntry("hbase.zookeeper.quorum",
                                                   "cdh-master-0,"
                                                   + "cdh-master-1,"
                                                   + "cdh-master-2")));
  }

  @Test
  public void testGetPropertyValue_correctJsonURIProperty_returnURIValue() throws Exception {
    String expected =
        "hdfs://localhost/cf/intel/instances/1cfe7b45-1e07-4751-a853-78ef47a313cc/";
    assertThatPropertyIsInExpectedLocation(PropertyLocator.HDFS_URI, expected);
  }

  @Test
  public void testGetPropertyValue_correctJsonNameSpaceProperty_returnHbaseNameSpaceValue()
      throws Exception {
    String expected = "6f1bb0fdab0502079c4c4ca6bc770574fe546fc1";
    assertThatPropertyIsInExpectedLocation(PropertyLocator.HBASE_NAMESPACE, expected);
  }

  @Test
  public void testGetPropertyValue_correctJsonKDCProperty_returnKDCValue() throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.KRB_KDC,
                                           "ip-10-10-9-198.us-west-2.compute.internal");
  }

  @Test
  public void testGetPropertyValue_correctJsonREALMProperty_returnREALMValue() throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.KRB_REALM, "US-WEST-2.COMPUTE.INTERNAL");
  }

  @Test
  public void testGetPropertyValue_correctJsonUSERProperty_returnUSERValue() throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.USER, "cf");
  }

  @Test
  public void testGetPropertyValue_correctJsonPASSWORDProperty_returnPASSWORDValue()
      throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.PASSWORD, "cf1");
  }

  @Test
  public void testGetPropertyValue_correctJsonZookeeperProperty_returnCLUSTERValue()
      throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.ZOOKEPER_URI, "0.0.0.0,1.1.1.1");
  }

  @Test
  public void testGetPropertyValue_correctJsonZookeeperProperty_returnZNODEValue()
      throws Exception {
    assertThatPropertyIsInExpectedLocation(PropertyLocator.ZOOKEPER_ZNODE,
                                           "/platform/e59a67b8-bcad-403e-a2a9-6bde5285f05e");
  }

  private void assertThatPropertyIsInExpectedLocation(PropertyLocator property,
                                                      String expectedValue) throws Exception {
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Optional<String> actual = helper.getPropertyFromJson(vcapServices, property);
    assertThat(actual.get(), equalTo(expectedValue));
  }

}