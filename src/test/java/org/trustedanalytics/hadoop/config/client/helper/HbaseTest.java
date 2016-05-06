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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectionFactory.class,
                 HBaseConfiguration.class,
                 UserProvider.class})
@PowerMockIgnore("javax.security.auth.*")
public class HbaseTest {

  private static final String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  private AppConfiguration appConfiguration;

  @Mock
  private KrbLoginManager loginManager;

  @Mock
  private UserProvider userProvided;

  @Mock
  private User user;

  @Before
  public void setUp() {
    try(InputStream hl = getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH)) {
      String vcapServices = IOUtils.toString(hl);
      appConfiguration = Configurations.newInstanceFromJson(vcapServices);
    } catch (IOException ignored) {
    }
  }

  @Test
  public void testCreateConfig_plentyOfInstancesHbase_returnConfigurationForInstanceThatHasGivenName()
      throws Exception {
    //given
    Hbase helper = Hbase.newInstanceForTests(HadoopClient.Builder.newInstance()
                                 .withAppConfiguration(appConfiguration)
                                 .withServiceName("hbase-instance1")
                                 .withLoginManager(loginManager));

    //when
    Configuration actual = helper.createConfig();
    String hbaseNamespace = helper.getServiceProperty(Property.HBASE_NAMESPACE);

    //then
    assertEquals(
        "cdh-master-0.node.gotapaaseu.consul,cdh-master-1.node.gotapaaseu.consul,cdh-master-2.node.gotapaaseu.consul",
        actual.get("hbase.zookeeper.quorum"));
    assertEquals("6f1bb0fdab0502079c4c4ca6bc770574fe546fc1", hbaseNamespace);
  }

  @Test
  public void testCreateConnection_simpleAuthMethodSet_() throws Exception {
    //given
    Hbase helper = Hbase.newInstanceForTests(HadoopClient.Builder.newInstance()
                                 .withAppConfiguration(appConfiguration)
                                 .withServiceName("hbase-instance1")
                                 .withLoginManager(loginManager));
    Configuration hadoopConf  = helper.createConfig();

    PowerMockito.mockStatic(ConnectionFactory.class);
    PowerMockito.mockStatic(HBaseConfiguration.class);
    PowerMockito.mockStatic(UserProvider.class);
    Mockito.when(HBaseConfiguration.create(any())).thenReturn(hadoopConf);
    Mockito.when(UserProvider.instantiate(hadoopConf)).thenReturn(userProvided);
    Mockito.when(userProvided.create(any())).thenReturn(user);

    //when
    helper.createConnection();

    //then
    PowerMockito.verifyStatic(Mockito.times(1));
    ConnectionFactory.createConnection(hadoopConf, user);
  }

  @Test
  public void testCreateConfig_namedKerberosInstance_returnConfigurationForInstanceThatHasGivenName()
      throws Exception {
    //given
    Hbase helper = Hbase.newInstanceForTests(HadoopClient.Builder.newInstance()
        .withAppConfiguration(appConfiguration)
        .withServiceName("hbase-instance1")
        .withKrbServiceName("kerberos-instance")
        .withLoginManager(loginManager));

    //when
    Configuration actual = helper.createConfig();
    String hbaseNamespace = helper.getServiceProperty(Property.HBASE_NAMESPACE);

    //then
    assertEquals(
        "cdh-master-0.node.gotapaaseu.consul,cdh-master-1.node.gotapaaseu.consul,cdh-master-2.node.gotapaaseu.consul",
        actual.get("hbase.zookeeper.quorum"));
    assertEquals("6f1bb0fdab0502079c4c4ca6bc770574fe546fc1", hbaseNamespace);
  }
}
