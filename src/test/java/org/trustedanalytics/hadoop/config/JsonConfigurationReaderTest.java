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

import org.junit.Test;
import org.trustedanalytics.hadoop.config.internal.ConfigNode;
import org.trustedanalytics.hadoop.config.internal.ConfigurationReader;
import org.trustedanalytics.hadoop.config.internal.JsonConfigurationReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


public class JsonConfigurationReaderTest {

  @Test(expected = NullPointerException.class)
  public void testGetReader_NullJsonString_throwsNLP() throws Exception {
    JsonConfigurationReader.getReader(null);
  }

  @Test
  public void testGetReader_correctJsonString_returnInitializedConfigReaderObj() throws Exception {
    String jsonString = "{\"root\" : {}}";
    ConfigurationReader reader = JsonConfigurationReader.getReader(jsonString);

    assertThat(reader, notNullValue());
  }

  @Test
  public void testRootNode_correctJsonString_returnInitializedConfigNodeObj() throws Exception {
    String jsonString = "{\"root\" : {}}";
    ConfigNode rootNode = JsonConfigurationReader.getReader(jsonString).getRootNode();

    assertThat(rootNode, notNullValue());
  }

}