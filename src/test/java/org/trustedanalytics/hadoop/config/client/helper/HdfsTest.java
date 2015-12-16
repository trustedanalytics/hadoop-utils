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
package org.trustedanalytics.hadoop.config.client.helper;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileSystem.class)
public class HdfsTest {

  private static final String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  private AppConfiguration appConfiguration;

  @Mock
  private KrbLoginManager loginManager;

  @Before
  public void setUp() {
    try(InputStream hl = getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH)) {
      String vcapServices = IOUtils.toString(hl);
      appConfiguration = Configurations.newInstanceFromJson(vcapServices);
    } catch (IOException ignored) {
    }
  }

  @Test
  public void testCreateFileSystem() throws Exception {
    //given
    Hdfs helper = Hdfs.newInstanceForTests(HadoopClient.Builder.newInstance()
                               .withAppConfiguration(appConfiguration)
                               .withServiceType(ServiceType.HDFS_TYPE)
                               .withLoginManager(loginManager));
    PowerMockito.mockStatic(FileSystem.class);

    //when
    helper.createFileSystem();

    //then
    PowerMockito.verifyStatic(Mockito.times(1));
    FileSystem.get(anyObject(), anyObject(), anyString());
  }

  @Test
  public void testCreateConfig_oneKerberizedInstanceHdfsServiceBound_returnConfiguration()
      throws Exception {
    //given
    Hdfs helper = Hdfs.newInstanceForTests(HadoopClient.Builder.newInstance()
                               .withAppConfiguration(appConfiguration)
                               .withServiceType(ServiceType.HDFS_TYPE)
                               .withLoginManager(loginManager));

    //when
    Configuration actual = helper.createConfig();

    //then
    verify(loginManager).loginWithCredentials("cf", "cf1".toCharArray());
    assertEquals("kerberos", actual.get("hadoop.security.authentication"));
    assertEquals("hdfs/_HOST@US-WEST-2.COMPUTE.INTERNAL",
                 actual.get("dfs.namenode.kerberos.principal"));
  }
}