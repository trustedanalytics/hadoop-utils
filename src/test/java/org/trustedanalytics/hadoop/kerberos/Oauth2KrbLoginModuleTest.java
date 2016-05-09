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

import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.hadoop.config.client.oauth.JwtToken;
import org.trustedanalytics.hadoop.config.client.oauth.TapOauthToken;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.Matchers.is;

public class Oauth2KrbLoginModuleTest {

  private JwtToken token;

  private String expectedKrbCacheLocation;

  private String anotherExpectedKrbCacheLocation;

  private static final String DEAFULT_REALM  = "CLOUDERA";

  @Before
  public void setUp() {
    //noinspection ConstantConditions
    this.token = new TapOauthToken(
         new FromFileTokenRetriver(
             Oauth2KrbLoginModuleTest.class.getClassLoader().getResource("oauth.token").getPath()
         ).get());
    System.setProperty(HadoopKrbLoginManager.KRB5_REALM, DEAFULT_REALM);
    this.expectedKrbCacheLocation = String.format("/tmp/%s@%s", token.getUserId(), DEAFULT_REALM);
    this.anotherExpectedKrbCacheLocation = "/tmp/jojo";
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(HadoopKrbLoginManager.KRB5_REALM);
    Files.deleteIfExists(Paths.get(this.expectedKrbCacheLocation));
    Files.deleteIfExists(Paths.get(this.anotherExpectedKrbCacheLocation));
  }

  @Test(expected = NullPointerException.class)
  public void testPrepareKrbCCache_useTokenTicketCacheNotSet_throwsNullPointerException()
      throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.getName(), "true");
    toTest.initialize(null, null, null, options);

    //when
    toTest.prepareKrbCCache(token);

    //then
    //throws NullPointerException
  }

  @Test
  public void testPrepareKrbCCache_givenTicketCachektinitSuccess_createCCacheFile()
      throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.KTINIT_COMMAND.getName(), ktinitPath("ktinit"));
    options.put(Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.getName(), "true");
    toTest.initialize(null, new Oauth2KrbCallbackHandler(this.token::getRawToken), null, options);

    //when
    toTest.prepareKrbCCache(token);

    //then
    Assert.assertTrue(Files.exists(Paths.get(this.expectedKrbCacheLocation)));
  }

  @Test
  public void testPrepareKrbCCache_noTicketCachektinitSuccess_createCCacheFile() throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.KTINIT_COMMAND.getName(), ktinitPath("ktinit"));
    options.put(Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.getName(), "true");
    options.put(Oauth2KrbLoginModule.ConfigOptions.TICKET_CACHE.getName(),
                this.anotherExpectedKrbCacheLocation);
    toTest.initialize(null, new Oauth2KrbCallbackHandler(this.token::getRawToken), null, options);

    //when
    toTest.prepareKrbCCache(token);

    //then
    Assert.assertTrue(Files.exists(Paths.get(this.anotherExpectedKrbCacheLocation)));
  }

  @Test(expected = IllegalStateException.class)
  public void testInitialize_ktinitFails_throwsIllegalStateException() throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.KTINIT_COMMAND.getName(),
                ktinitPath("ktinit_that_fails"));
    options.put(Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.getName(), "true");

    //when
    toTest.initialize(null, new Oauth2KrbCallbackHandler(this.token::getRawToken), null, options);

    //then
    //throws IllegalStateException
  }

  @Test(expected = IllegalStateException.class)
  public void testInitialize_ktinitSuccessNoKrbccache_throwsIllegalStateException()
      throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.KTINIT_COMMAND.getName(),
                ktinitPath("ktinit_that_does_not_produce_krbccache"));
    options.put(Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.getName(), "true");

    //when
    toTest.initialize(null, new Oauth2KrbCallbackHandler(this.token::getRawToken), null, options);

    //then
    //throws IllegalStateException
  }

  @Test
  public void testPrepareOptionsForDelegation() throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put("principal", "jojo");
    options.put("storeKey", "true");
    options.put("doNotPrompt", "false");
    options.put("renewTGT", "false");
    options.put("refreshKrb5Config", "false");
    options.put("isInitiator", "false");
    options.put("clearPass", "true");
    //when

    Map actual = toTest.prepareOptionsForDelegation(this.token, options);

    //then
    Assert.assertThat(actual.get("principal"), is("97c4b711-7c3b-45f7-b06e-c671c5915b27"));
    Assert.assertThat(actual.get("storeKey"), is("true"));
    Assert.assertThat(actual.get("doNotPrompt"), is("false"));
    Assert.assertThat(actual.get("renewTGT"), is("false"));
    Assert.assertThat(actual.get("refreshKrb5Config"), is("false"));
    Assert.assertThat(actual.get("isInitiator"), is("false"));
    Assert.assertThat(actual.get("clearPass"), is("true"));
    Assert.assertThat(actual.get("useTicketCache"), is("true"));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testRetrieveToken_setTokenCacheNotSetTokenRetriever_returnToken() throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();
    options.put(Oauth2KrbLoginModule.ConfigOptions.TOKEN_CACHE.getName(),
                Oauth2KrbLoginModuleTest.class.getClassLoader().getResource("oauth.token").getPath());
    //when
    JwtToken actual = toTest.retrieveToken(new Oauth2KrbCallbackHandler(), options);

    //then
    Assert.assertThat(actual.getRawToken(), is(this.token.getRawToken()));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testRetrieveToken_notSetTokenCacheSetTokenRetriever_returnToken() throws Exception {
    //given
    Oauth2KrbLoginModule toTest = new Oauth2KrbLoginModule();
    Map<String, String> options = Maps.newHashMap();

    //when
    JwtToken actual = toTest.retrieveToken(
        new Oauth2KrbCallbackHandler(this.token::getRawToken), options);

    //then
    Assert.assertThat(actual.getRawToken(), is(this.token.getRawToken()));
  }

  @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
  private String ktinitPath(String scriptName) {
    String ktinit = Oauth2KrbLoginModuleTest.class
        .getClassLoader().getResource("krb5jwt/bin/" + scriptName).getPath();
    new File(ktinit).setExecutable(true);
    return ktinit;
  }
}