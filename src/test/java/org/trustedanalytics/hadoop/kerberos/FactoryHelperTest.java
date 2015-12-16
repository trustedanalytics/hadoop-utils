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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trustedanalytics.hadoop.config.client.oauth.TapOauthToken;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

public class FactoryHelperTest {

  private static String userDir;

  private final HadoopKrbLoginManager.FactoryHelper factoryHelper =
      new HadoopKrbLoginManager.FactoryHelper();

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException {
    userDir = System.getProperty("user.dir");
    String testsDir = FactoryHelperTest.class.getClassLoader().getResource("").getPath();
    File ktinit =
        new File(FactoryHelperTest.class.getClassLoader().getResource("bin/ktinit").toURI());
    if (!ktinit.setExecutable(true, true)) {
      throw new IllegalStateException("Can't initialize unit tests suite!");
    }
    System.setProperty("user.dir", testsDir);
    System.setProperty(HadoopKrbLoginManager.KRB5_REALM, "CLOUDERA");
  }

  @AfterClass
  public static void afterTests() {
    System.setProperty("user.dir", userDir);
    System.clearProperty(HadoopKrbLoginManager.KRB5_REALM);
  }

  @Test(expected = IllegalStateException.class)
  public void testCacheKrbCredentials_incorrectJwtToken_throwsException() throws Exception {
    factoryHelper.cacheKrbCredentials(new TapOauthToken("someincorrecttoken"));
  }

  @Test(expected = LoginException.class)
  public void testCacheKrbCredentials_correctJwtTokenKtinitExitCode1_throwsException()
      throws Exception {
    String token = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL2p3dC1pZHAuZXhhbXBs"
                      + "ZS5jb20iLCJzdWIiOiJtYWlsdG86bWlrZUBleGFtcGxlLmNvbSIsIm5iZiI6MTQ1MDcwMzQxOS"
                      + "wiZXhwIjoxNDUwNzA3MDE5LCJpYXQiOjE0NTA3MDM0MTksImp0aSI6ImlkMTIzNDU2IiwidHlw"
                      + "IjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9yZWdpc3RlciJ9.";
    factoryHelper.cacheKrbCredentials(new TapOauthToken(token));
  }

  @Test
  public void testCacheKrbCredentials_correctJwtTokenKtinitExitCode0_ranQuietly() throws Exception {
    String token = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI2ZWY2MzI1MC05MmZiLTQzOWYtYjJhYy05ODZkMDdhNGQ"
                      + "1NjMiLCJzdWIiOiJjOTg1M2MxMi1jYzQyLTRkN2YtOGFhOS1iM2ZmYTZiMTc1MDciLCJzY29wZ"
                      + "SI6WyJwYXNzd29yZC53cml0ZSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb2xsZXIud3JpdGUiLCJ"
                      + "jbG91ZF9jb250cm9sbGVyLnJlYWQiXSwiY2xpZW50X2lkIjoiY2YiLCJjaWQiOiJjZiIsImF6c"
                      + "CI6ImNmIiwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwidXNlcl9pZCI6ImM5ODUzYzEyLWNjNDI"
                      + "tNGQ3Zi04YWE5LWIzZmZhNmIxNzUwNyIsInVzZXJfbmFtZSI6ImFydHVyIiwiZW1haWwiOiJhc"
                      + "nR1ciIsInJldl9zaWciOiI0YTU3ZjJjYiIsImlhdCI6MTQ1MDY4NjUyMiwiZXhwIjoxNDUwNjg"
                      + "3MTIyLCJpc3MiOiJodHRwczovL3VhYS5tZWdhY2xpdGUuZ290YXBhYXMuZXUvb2F1dGgvdG9rZ"
                      + "W4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsib3BlbmlkIiwiY2xvdWRfY29udHJvbGxlciIsInBhc3N"
                      + "3b3JkIiwiY2YiXX0.";
    factoryHelper.cacheKrbCredentials(new TapOauthToken(token));
  }

}