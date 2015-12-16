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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HadoopKrbLoginManagerTest {

  private final String kdc = "addr";

  private final String realm = "@realm";

  private final String login = "some";

  private final String pathToKeyTab = "/some/path";

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
    toTest.getUserName((Subject) null);
  }

  @Test
  public void testGetUserName_jwtToken_returnUserName() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    String actual = toTest.getUserName(
        "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI1OWViZTBjOC1kYzE4LTQwNGQtOTJlOC01MmQzMWEwZW"
        + "RkYjQiLCJzdWIiOiJhNTA5YjQ4OC05NTQ4LTRlNDItOTA2Yi1hZWU0MjBlM2FmMmYiLCJzY29"
        + "wZSI6WyJzY2ltLnJlYWQiLCJjb25zb2xlLmFkbWluIiwiY2xvdWRfY29udHJvbGxlci5hZG1p"
        + "biIsInBhc3N3b3JkLndyaXRlIiwic2NpbS53cml0ZSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb"
        + "2xsZXIud3JpdGUiLCJjbG91ZF9jb250cm9sbGVyLnJlYWQiLCJkb3BwbGVyLmZpcmVob3NlIl"
        + "0sImNsaWVudF9pZCI6ImNmIiwiY2lkIjoiY2YiLCJhenAiOiJjZiIsImdyYW50X3R5cGUiOiJ"
        + "wYXNzd29yZCIsInVzZXJfaWQiOiJhNTA5YjQ4OC05NTQ4LTRlNDItOTA2Yi1hZWU0MjBlM2Fm"
        + "MmYiLCJ1c2VyX25hbWUiOiJhZG1pbiIsImVtYWlsIjoiYWRtaW4iLCJyZXZfc2lnIjoiOWY1O"
        + "GE1OTkiLCJpYXQiOjE0NTAzNDE5ODMsImV4cCI6MTQ1MDM0MjU4MywiaXNzIjoiaHR0cHM6Ly"
        + "91YWEubWVnYWNsaXRlLmdvdGFwYWFzLmV1L29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXV"
        + "kIjpbImRvcHBsZXIiLCJzY2ltIiwiY29uc29sZSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb2xs"
        + "ZXIiLCJwYXNzd29yZCIsImNmIl19.be7j33CimvH8WDDXU5Z84mVNPgq_aUwMCWFJrbwqW6Nb"
        + "SOSupb9dxe7TXxuas7MuQAmhgpCwqV3L0zsx0Yhrcf1XDNITlTT1NIkwMx0swh8CAArsKJG6m"
        + "mjg6LYxnFL0IhYe0Ak3F4HduQrkHKCyzg5cRT7htrNSQvpcAPyU08c");
    String expected = "admin";
    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUserName_noPrincipalInSubject_throwsException() throws Exception {
    HadoopKrbLoginManager.FactoryHelper helper = mock(HadoopKrbLoginManager.FactoryHelper.class);
    HadoopKrbLoginManager toTest = new HadoopKrbLoginManager(kdc, realm, helper);
    Subject subject = new Subject();
    toTest.getUserName(subject);
  }

}