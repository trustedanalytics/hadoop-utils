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

import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import sun.security.krb5.PrincipalName;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class HadoopKrbLoginManagerTest {

  private String kdc = "addr";

  private String realm = "@realm";

  private String login = "some";

  private String pathToKeyTab = "/some/path";

  private char[] pass = "password".toCharArray();

  @Test
  public void testSetKerbConfigFromOpts_someOptionSet_appConfigurationEntrySet() throws Exception {
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm);
    Map<String, String> opts = new HashMap<>();
    opts.put("keyTab", pathToKeyTab);
    toTest.setKerbConfigFromOpts(login, opts);

    AppConfigurationEntry[] keyTabEntry = Configuration.getConfiguration()
        .getAppConfigurationEntry(login);
    Map<String, ?> keyTab = keyTabEntry[0].getOptions();

    assertThat(keyTab, IsMapContaining.hasEntry("keyTab", pathToKeyTab));
  }
  
  @Test
  public void testLoginWithCredentials_givenLoginAndPass_callingKrbSubject() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    LoginContext lc = mock(LoginContext.class);

    when(helper.getLoginContext(anyString(), anyObject())).thenReturn(lc);

    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    toTest.loginWithCredentials("login", "pass".toCharArray());

    verify(lc).login();
    verify(lc).getSubject();
  }

  @Test
  public void testLoginWithKeyTab_givenLoginAndKeyTab_callingKrbSubject() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    LoginContext lc = mock(LoginContext.class);

    when(helper.getLoginContext(anyString())).thenReturn(lc);

    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    toTest.loginWithKeyTab(login, pathToKeyTab);

    verify(lc).login();
    verify(lc).getSubject();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateParams_nullKdc_throwsException() throws Exception {
    HadoopKrbLoginManager.validateParams(null, realm);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateParams_emptyKdc_throwsException() throws Exception {
    HadoopKrbLoginManager.validateParams("", realm);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateParams_nullRealm_throwsException() throws Exception {
    HadoopKrbLoginManager.validateParams(kdc, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateParams_emptyRealm_throwsException() throws Exception {
    HadoopKrbLoginManager.validateParams(kdc, "");
  }

  @Test(expected = NullPointerException.class)
  public void testGetUGI_nullSubject_throwsException() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    toTest.getUGI(null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetUserName_nullSuject_throwsException() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    toTest.getUserName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUserName_noPrincipalInSubject_throwsException() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    Subject subject = new Subject();
    toTest.getUserName(subject);
  }

}