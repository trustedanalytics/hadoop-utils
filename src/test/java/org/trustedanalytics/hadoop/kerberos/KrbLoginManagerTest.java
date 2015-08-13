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
package org.trustedanalytics.hadoop.kerberos;

import com.google.common.base.Preconditions;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.minikdc.MiniKdc;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import javax.security.auth.Subject;

public class KrbLoginManagerTest {

  private static MiniKdc kdc;

  private static String PRINCIPAL_NAME = "jojo";

  private static String ANOTHER_PRINCIPAL_NAME = "anotherJojo";

  private static String PRINCIPAL_PASS = "jojo1";

  private static File keyTabForAnotherJojo;

  @BeforeClass
  public static void startUp() throws Exception {
    cleanEnv();
    File testDir = createTestDir();
    kdc = new MiniKdc(createMiniKdcConf(), testDir);
    kdc.start();
    keyTabForAnotherJojo = new File(testDir, "test.keytab");
    kdc.createPrincipal(PRINCIPAL_NAME, PRINCIPAL_PASS);
    kdc.createPrincipal(keyTabForAnotherJojo, ANOTHER_PRINCIPAL_NAME);
  }

  @AfterClass
  public static void tearDown() {
    Preconditions.checkState(kdc != null);
    kdc.stop();
  }

  @Test
  public void testLoginWithCredentials_validCredentials_successLogin() throws Exception {
    KrbLoginManager loginManager = new HadoopKrbLoginManager(kdc.getHost() + ":" + kdc.getPort(),
                                           kdc.getRealm());

    Subject subject = loginManager.loginWithCredentials(PRINCIPAL_NAME,
                                                        PRINCIPAL_PASS.toCharArray());

    Assert.assertEquals(1, subject.getPrincipals().size());
    Assert.assertEquals(PRINCIPAL_NAME + "@" + kdc.getRealm(),
                        subject.getPrincipals().iterator().next().getName());
  }

  @Test
  public void testLoginWithKeyTab_validKeyTabCred_successLogin() throws Exception {
    KrbLoginManager loginManager = new HadoopKrbLoginManager(kdc.getHost() + ":" + kdc.getPort(),
                                                             kdc.getRealm());

    Subject subject = loginManager.loginWithKeyTab(ANOTHER_PRINCIPAL_NAME,
                                                   keyTabForAnotherJojo.getPath());

    Assert.assertEquals(1, subject.getPrincipals().size());
    Assert.assertEquals(ANOTHER_PRINCIPAL_NAME + "@" + kdc.getRealm(),
                        subject.getPrincipals().iterator().next().getName());
  }

  @Test
  public void testLoginInHadoop_validKeyTabCred_ProperlyInitializedUGI() throws Exception {
    KrbLoginManager loginManager = new HadoopKrbLoginManager(kdc.getHost() + ":" + kdc.getPort(),
                                                             kdc.getRealm());

    Subject subject = loginManager.loginWithKeyTab(ANOTHER_PRINCIPAL_NAME,
                                                   keyTabForAnotherJojo.getPath());
    Configuration hadoopConf = new Configuration(true);
    loginManager.loginInHadoop(subject, hadoopConf);

    Assert.assertTrue(UserGroupInformation.isLoginKeytabBased());
  }

  private static File createTestDir() {
    return new File("target");
  }

  private static Properties createMiniKdcConf() {
    Properties conf = MiniKdc.createConf();
    conf.put(MiniKdc.DEBUG, true);
    return conf;
  }

  private static void cleanEnv() {
    System.getProperties().remove(HadoopKrbLoginManager.KRB5_CONF);
    System.getProperties().remove(HadoopKrbLoginManager.KRB5_KDC);
    System.getProperties().remove(HadoopKrbLoginManager.KRB5_REALM);
  }
}