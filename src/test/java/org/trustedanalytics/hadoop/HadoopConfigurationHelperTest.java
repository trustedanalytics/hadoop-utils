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
package org.trustedanalytics.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.trustedanalytics.hadoop.config.ConfigConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class HadoopConfigurationHelperTest {

  private String jsonStringConf;

  public HadoopConfigurationHelperTest(String conf) {
    this.jsonStringConf = conf;
  }

  @Parameterized.Parameters()
  public static Collection configuration() {
    return Arrays.asList(new Object[][]{
        {"{\n"
         + " \"VCAP_SERVICES\":{\n"
         + "  \"hdfs\":[\n{"
         + "   \"credentials\": {"
         + "      \"" + ConfigConstants.HADOOP_CONFIG_KEY_VALUE + "\":{\n"
         + "       \"hadoop.security.authentication\":\"Kerberos\",\n"
         + "       \"dfs.namenode.kerberos.principal\":\"hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL\"\n"
         + "     }\n"
         + "    }\n"
         + "   }],\n"
         + "   \"yarn\": [\n{"
         + "    \"credentials\": {"
         + "     \"" + ConfigConstants.HADOOP_CONFIG_KEY_VALUE + "\":{\n"
         + "       \"yarn.resourcemanager.hostname\": \"0.0.0.0\"\n"
         + "     }\n"
         + "    }\n"
         + "  }]\n"
         + " }\n"
         + "}\n"},
    });
  }

  @Test
  public void testGetHadoopConfFromJson_correctConfJSON_returnAllConfProps()
      throws Exception {
    Optional<Map<String, String>> configParams = HadoopConfigurationHelper
        .getHadoopConfFromJson(jsonStringConf);

    assertThat(configParams.get().entrySet(), allOf(hasSize(3)));
    assertThat(configParams.get(), allOf(hasEntry("yarn.resourcemanager.hostname",
                                                           "0.0.0.0"),
                                         hasEntry("hadoop.security.authentication", "Kerberos"),
                                         hasEntry("dfs.namenode.kerberos.principal",
                                                  "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test
  public void testGetHadoopConfFromJson_correctEmptyConfJSON_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_SERVICES\":{}}";
    Optional<Map<String, String>> op = HadoopConfigurationHelper.getHadoopConfFromJson(jsonConf);

    Map<String, String> expected = new HashMap<>();

    assertThat(op.get(), equalTo(expected));
  }

  @Test
  public void testGetHadoopConfFromJson_correctJSONNoConf_returnEmptyMap() throws Exception {
    String jsonConf = "{\"VCAP_APPLICATION\":{}}";
    Optional<Map<String, String>> op = HadoopConfigurationHelper.getHadoopConfFromJson(jsonConf);

    Map<String, String> expected = new HashMap<>();

    assertThat(op.get(), equalTo(expected));
  }

  @Test(expected = IOException.class)
  public void testGetHadoopConfFromJson_incorrectJSON_throwsException() throws Exception {
    String incorrectJsonConf = "{\"VCAP_APPLICATION\":{}";
    HadoopConfigurationHelper.getHadoopConfFromJson(incorrectJsonConf);
  }

  @Test
  public void testGetHadoopConfByServiceName_correctConfJSONAndServiceName_returnChoosenServiceConfProps()
      throws Exception {
    Optional<Map<String, String>> configParams =
        HadoopConfigurationHelper.getHadoopConfByServiceName(jsonStringConf, HadoopServiceType.HDFS);

    assertThat(configParams.get().entrySet(), allOf(hasSize(2)));
    assertThat(configParams.get(), allOf(hasEntry("hadoop.security.authentication", "Kerberos"),
                                         hasEntry("dfs.namenode.kerberos.principal",
                                                  "hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetHadoopConfFromEnv_envVariableIsNotSet_throwsException() throws Exception {
    Optional<Map<String, String>> actual =
        HadoopConfigurationHelper.getHadoopConfFromEnv(HadoopConfigurationHelper.VCAP_SERVICES);
  }

  @Test
  public void testMergeConfiguration_newConfCollectionOfParamsToMerge_mergedConf() {
    Configuration conf = new Configuration(false);

    Map<String, String> toMerge = new HashMap<>();
    toMerge.put("hadoop.security.authentication", "Kerberos");
    HadoopConfigurationHelper.mergeConfiguration(conf, toMerge);

    assertThat(conf.get("hadoop.security.authentication"), equalTo("Kerberos"));
  }

  @Test
  public void testMergeConfiguration_newValueForSetParam_overwroteParamValue() {
    Configuration conf = new Configuration(false);
    conf.set("hadoop.security.authentication", "simple");

    Map<String, String> toMerge = new HashMap<>();
    toMerge.put("hadoop.security.authentication", "Kerberos");
    HadoopConfigurationHelper.mergeConfiguration(conf, toMerge);

    assertThat(conf.get("hadoop.security.authentication"), equalTo("Kerberos"));
  }

  @Test
  public void testMergeConfiguration_mapOfPramsIsNull_nothigChangedInConf() {
    Configuration conf = new Configuration();
    conf.set("hadoop.security.authentication", "simple");
    Configuration expected = new Configuration(conf); //clone of initial Configuration

    HadoopConfigurationHelper.mergeConfiguration(conf, null);

    assertThat(conf.size(), equalTo(expected.size()));
  }


}