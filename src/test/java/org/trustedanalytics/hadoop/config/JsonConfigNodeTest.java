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
import org.trustedanalytics.hadoop.config.internal.ConfigNode;
import org.trustedanalytics.hadoop.config.internal.JsonConfigurationReader;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JsonConfigNodeTest {

  ConfigNode root;

  @Before
  public void setUp() throws Exception {
    JsonConfigurationReader reader = JsonConfigurationReader.getReader(IOUtils.toString(
        getClass().getResourceAsStream("/test.json")));
    root = reader.getRootNode();
  }

  @Test
  public void testGetChildren_arrayOfChildren_returnListConfigNodes() throws Exception {
    List<ConfigNode> children =  root.find("hdfs").getChildren();

    assertThat(children.size(), equalTo(2));
    assertThat(children.get(0).name(), equalTo("instance1"));
    assertThat(children.get(1).name(), equalTo("instance2"));
  }

  @Test
  public void testGetChildren_returnListConfigNodes() throws Exception {
    List<ConfigNode> children = root.get("VCAP_SERVICES").getChildren();

    assertThat(children.size(), equalTo(2));
    assertThat(children.get(0).name(), equalTo("hdfs"));
    assertThat(children.get(1).name(), equalTo("yarn"));
  }

  @Test
  public void testFind_existentNode_returnConfNodeObj() throws Exception {
    ConfigNode node = root.find("param3");

    assertThat(node, notNullValue());
  }

  @Test(expected = NullPointerException.class)
  public void testFind_notExistentNode_throwsException() throws Exception {
    root.find("not_existent_param_name");
  }

  @Test
  public void testGet_existentNode_returnConfNodeObj() throws Exception {
    ConfigNode node = root.get("VCAP_SERVICES").get("hdfs");

    assertThat(node, notNullValue());
  }

  @Test(expected = NullPointerException.class)
  public void testGet_notExistentNode_throwsException() throws Exception {
    root.get("VCAP_SERVICES").get("jojo");
  }

  @Test
  public void testValue_existentValueNode_() throws Exception {
    String node = root.get("VCAP_SERVICES").get("hdfs").find("param1").value();

    assertThat(node, equalTo("value_param1"));
  }

  @Test
  public void testFindAll_existentNodes_returnListOfConfigNodes() throws Exception {
    List<ConfigNode> nodes = root.findAll("configuration");

    assertThat(nodes.size(), equalTo(3));
  }

  @Test
  public void testFindAll_notExistentNodes_returnEmptyList() throws Exception {
    List<ConfigNode> sections = root.findAll("not_existent_node_name");

    assertThat(sections, empty());
  }

  @Test
  public void testSelectOne_ExistentInstance_returnNodeForSelectedInstance() throws Exception {
    String node = root.get("VCAP_SERVICES").get("hdfs").selectOne("name","instance2")
        .find("param3").value();

    assertThat(node, equalTo("value_param3"));
  }

  @Test(expected = NullPointerException.class)
  public void testSelectOne_NotExistentInstance_throwsException() throws Exception {
    root.get("VCAP_SERVICES").get("hdfs").selectOne("name","not_existent_instance_name");
  }

  @Test(expected = NullPointerException.class)
  public void testSelectOne_NotTextualNode_throwsException() throws Exception {
    root.get("VCAP_SERVICES").get("hdfs").selectOne("configuration","some_value");
  }
}