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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Hive.class, DriverManager.class})
@PowerMockIgnore("javax.security.auth.*")
public class HiveTest {

  private static final String ENV_VCAP_SERVICES_FILE_PATH = "/env_vcap_service.json";

  private AppConfiguration appConfiguration;

  @Mock
  private KrbLoginManager loginManager;

  @Mock
  private Connection connection;

  @Mock
  private JwtToken token;


  @Before
  public void setUp() {
    try (InputStream hl = getClass().getResourceAsStream(ENV_VCAP_SERVICES_FILE_PATH)) {
      String vcapServices = IOUtils.toString(hl);
      appConfiguration = Configurations.newInstanceFromJson(vcapServices);
    } catch (IOException ignored) {
    }
  }

  @Test
  public void testCreateConnectionString_setDatabase_withName() throws Exception {
    Hive helper = Hive.newInstanceForTests(
        HadoopClient.Builder.newInstance().withAppConfiguration(appConfiguration)
            .withServiceName("hive-instance1").withLoginManager(loginManager));

    String expectedUri =
        "jdbc:hive2://cdh-master-0.node.gotapaaseu.consul:10000/myName;kerberosAuthType=fromSubject";

    assertEquals(expectedUri, helper.getConnectionString("myName"));
  }

  @Test
  public void testCreateConnectionString_setDatabase_withoutName() throws Exception {
    Hive helper = Hive.newInstanceForTests(
        HadoopClient.Builder.newInstance().withAppConfiguration(appConfiguration)
            .withServiceName("hive-instance1").withLoginManager(loginManager));

    String expectedUri =
        "jdbc:hive2://cdh-master-0.node.gotapaaseu.consul:10000/;kerberosAuthType=fromSubject";

    assertEquals(expectedUri, helper.getConnectionString());
  }

  @Test
  public void testCreateConnection_simpleAuth_() throws Exception {
    Hive helper = Hive.newInstanceForTests(
        HadoopClient.Builder.newInstance().withAppConfiguration(appConfiguration)
            .withServiceName("hive-instance1").withLoginManager(loginManager));

    PowerMockito.mockStatic(Hive.class);
    PowerMockito.when(Hive.getConnection(Matchers.any(), Matchers.any(), Matchers.eq(helper.getConnectionString(""))))
        .thenReturn(connection);

    assertEquals(helper.getConnection(""), connection);
    Mockito.verify(loginManager, Mockito.times(0)).loginWithCredentials(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testCreateConnection_simpleAuth_withJWT() throws Exception {
    Hive helper = Hive.newInstanceForTests(
        HadoopClient.Builder.newInstance().withAppConfiguration(appConfiguration)
            .withServiceName("hive-instance1").withLoginManager(loginManager));

    String userID = "super-fake-id";
   Mockito.when(token.getUserId()).thenReturn(userID);

    PowerMockito.mockStatic(Hive.class);
    PowerMockito.when(Hive.getConnection(Matchers.eq(userID), Matchers.any(), Matchers.eq(helper.getConnectionString(""))))
        .thenReturn(connection);

    assertEquals(helper.getConnection(token, ""), connection);
    Mockito.verify(loginManager, Mockito.times(0)).loginWithCredentials(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testCreateConnection_namedKerberosInstance_() throws Exception {
    Hive helper = Hive.newInstanceForTests(HadoopClient.Builder.newInstance()
        .withAppConfiguration(appConfiguration).withServiceName("hive-instance2")
        .withKrbServiceName("kerberos-instance").withLoginManager(loginManager));

    PowerMockito.mockStatic(Hive.class);
    PowerMockito.when(Hive.getConnection(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(connection);

    assertEquals(helper.getConnection(""), connection);
    Mockito.verify(loginManager, Mockito.times(1)).loginWithCredentials(Mockito.any(),
        Mockito.any());
  }


  @Test
  public void testCreateConnection_namedKerberosInstance_withJWT() throws Exception {
    Hive helper = Hive.newInstanceForTests(HadoopClient.Builder.newInstance()
        .withAppConfiguration(appConfiguration).withServiceName("hive-instance2")
        .withKrbServiceName("kerberos-instance").withLoginManager(loginManager));

    String userID = "super-fake-id";
    Mockito.when(token.getUserId()).thenReturn(userID);

    PowerMockito.mockStatic(Hive.class);
    PowerMockito.when(Hive.getConnection(Matchers.eq(userID), Matchers.any(), Matchers.eq(helper.getConnectionString(""))))
        .thenReturn(connection);

    assertEquals(helper.getConnection(token, ""), connection);
    Mockito.verify(loginManager, Mockito.times(1)).loginWithJWTtoken(token);
  }

  @Test
  public void testCreateConnection_connectionCreation_() throws  Exception{
    Hive helper = Hive.newInstanceForTests(HadoopClient.Builder.newInstance()
            .withAppConfiguration(appConfiguration).withServiceName("hive-instance1"));
    String connectionUrl = "jdbc:hive2://cdh-master-0.node.gotapaaseu.consul:10000/;kerberosAuthType=fromSubject";
    Configuration config = helper.createConfig();

    PowerMockito.mockStatic(DriverManager.class);
    PowerMockito.when(DriverManager.getConnection(connectionUrl, null, null)).thenReturn(connection);

    assertEquals(Hive.getConnection("Hive", config, connectionUrl), connection);
  }
}
