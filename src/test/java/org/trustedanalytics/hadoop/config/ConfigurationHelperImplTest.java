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
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConfigurationHelperImplTest {

  private static String CF_ENV_FILE_PATH = "/vcap.json";

  private static String CF_VCAP_SERVICES_FILE_PATH = "/vcap_service.json";

  private static String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  @Test
  public void testGetInstance() throws Exception {

  }

  @Test
  public void testGetConfFromJson_correctJsonKrbConfiguration_returnKRBConfParams() throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_ENV_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> conf = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.KRB);

    Map<String, String> expected = new HashMap<>();
    expected.put("kdc", "ip-10-10-9-198.us-west-2.compute.internal");
    expected.put("krealm", "US-WEST-2.COMPUTE.INTERNAL");

    assertThat(conf, equalTo(expected));
  }

  @Test
  public void testGetConfFromJson_correctConfJSON_returnAllConfProps()
      throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_VCAP_SERVICES_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.HADOOP);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(3)));
    Assert.assertThat(configParams, allOf(hasEntry("yarn.resourcemanager.hostname",
                    "0.0.0.0"),
            hasEntry("hadoop.security.authentication", "Kerberos"),
            hasEntry("dfs.namenode.kerberos.principal",
                    "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test
  public void testGetConfFromJson_correctEmptyConfJSON_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_SERVICES\":{}}";
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> actual = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.HADOOP);

    Map<String, String> expected = new HashMap<>();

    Assert.assertThat(actual, equalTo(expected));
  }

  @Test
  public void testGetConfFromJson_correctJSONNoVcapServicesConf_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_APPLICATION\":{}}";
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> actual = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.HADOOP);

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
    helper.getConfigurationFromEnv("not_set_env_variable_name", ConfigurationLocator.KRB);
  }

  @Test
  public void testGetConfByServiceName_correctConfJSONAndServiceName_returnHDFSServiceConfProps()
      throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_VCAP_SERVICES_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.HDFS);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(2)));
    Assert.assertThat(configParams, allOf(hasEntry("hadoop.security.authentication", "Kerberos"),
            hasEntry("dfs.namenode.kerberos.principal",
                    "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test
  public void testGetConfByServiceName_correctConfJSONAndServiceName_returnYARNServiceConfProps()
      throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_VCAP_SERVICES_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Map<String, String> configParams = helper.getConfigurationFromJson(jsonConf, ConfigurationLocator.YARN);

    Assert.assertThat(configParams.entrySet(), allOf(hasSize(1)));
    Assert.assertThat(configParams, allOf(hasEntry("yarn.resourcemanager.hostname", "0.0.0.0")));
  }

  @Test
  public void testGetPropertyValue_correctJsonURIProperty_returnURIValue() throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_ENV_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Optional<String> uri = helper.getPropertyFromJson(jsonConf, PropertyLocator.HDFS_URI);

    String expected =
        "hdfs://nameservice1/cf/intel//instances/1cfe7b45-1e07-4751-a853-78ef47a313cc/";
    assertThat(uri.get(), equalTo(expected));
  }

  @Test
  public void testGetPropertyValue_correctJsonKDCProperty_returnKDCValue() throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_ENV_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Optional<String> uri = helper.getPropertyFromJson(jsonConf, PropertyLocator.KRB_KDC);

    String expected =
        "ip-10-10-9-198.us-west-2.compute.internal";
    assertThat(uri.get(), equalTo(expected));
  }

  @Test
  public void testGetPropertyValue_correctJsonREALMProperty_returnREALMValue() throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(CF_ENV_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Optional<String> uri = helper.getPropertyFromJson(jsonConf, PropertyLocator.KRB_REALM);

    String expected =
        "US-WEST-2.COMPUTE.INTERNAL";
    assertThat(uri.get(), equalTo(expected));
  }

  @Test
  public void testGetPropertyValue_correctJsonZookeeperProperty_returnCLUSTERValue() throws Exception {
    String jsonConf = IOUtils.toString(getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH));
    ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
    Optional<String> uri = helper.getPropertyFromJson(jsonConf, PropertyLocator.ZOOKEPER_URI);

    String expected =
            "0.0.0.0,1.1.1.1";
    assertThat(uri.get(), equalTo(expected));
  }


}