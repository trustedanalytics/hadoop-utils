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
import org.trustedanalytics.hadoop.config.internal.ConfigPath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;


public class ConfigPathTest {

  @Test
  public void testAppend_givenPathAndPathToAppend_appendPathOnTheEndOfGiven() throws Exception {
    //given
    ConfigPath path1 = ConfigPath.createPath();
    path1.add(configNode -> configNode.findAll("node1"));
    path1.add(configNode -> configNode.findAll("node2"));
    ConfigPath path2 = ConfigPath.createPath();
    path2.add(configNode -> configNode.findAll("node3"));

    //when
    path1.append(path2);

    //then
    assertThat(path1.getStack(), allOf(hasSize(3)));
  }

  @Test
  public void testAdd_() throws Exception {
    //given
    ConfigPath path = ConfigPath.createPath();

    //when
    path.add(configNode -> configNode.findAll("node1")).
        add(configNode1 -> configNode1.findAll("node2"));

    //then
    assertThat(path.getStack(), allOf(hasSize(2)));
  }

}