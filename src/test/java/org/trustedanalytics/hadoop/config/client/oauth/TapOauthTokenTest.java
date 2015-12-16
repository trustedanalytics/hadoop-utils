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
package org.trustedanalytics.hadoop.config.client.oauth;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TapOauthTokenTest {

  private static final String token =
      "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI2ZWY2MzI1MC05MmZiLTQzOWYtYjJhYy05ODZkMDdhNGQ"
      + "1NjMiLCJzdWIiOiJjOTg1M2MxMi1jYzQyLTRkN2YtOGFhOS1iM2ZmYTZiMTc1MDciLCJzY29wZ"
      + "SI6WyJwYXNzd29yZC53cml0ZSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb2xsZXIud3JpdGUiLCJ"
      + "jbG91ZF9jb250cm9sbGVyLnJlYWQiXSwiY2xpZW50X2lkIjoiY2YiLCJjaWQiOiJjZiIsImF6c"
      + "CI6ImNmIiwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwidXNlcl9pZCI6ImM5ODUzYzEyLWNjNDI"
      + "tNGQ3Zi04YWE5LWIzZmZhNmIxNzUwNyIsInVzZXJfbmFtZSI6ImFydHVyIiwiZW1haWwiOiJhc"
      + "nR1ciIsInJldl9zaWciOiI0YTU3ZjJjYiIsImlhdCI6MTQ1MDY4NjUyMiwiZXhwIjoxNDUwNjg"
      + "3MTIyLCJpc3MiOiJodHRwczovL3VhYS5tZWdhY2xpdGUuZ290YXBhYXMuZXUvb2F1dGgvdG9rZ"
      + "W4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsib3BlbmlkIiwiY2xvdWRfY29udHJvbGxlciIsInBhc3N"
      + "3b3JkIiwiY2YiXX0.";

  @Test
  public void testGetUserName_correctJWTtoken_returnsDecodedTapUserName() throws Exception {
    JwtToken tapToken = new TapOauthToken(token);
    assertThat(tapToken.getUserName(), is("artur"));
  }

  @Test
  public void testGetRawToken_correctJWTtoken_returnsRawToken() throws Exception {
    JwtToken tapToken = new TapOauthToken(token);
    assertThat(tapToken.getRawToken(), is(token));
  }

  @Test(expected = IllegalStateException.class)
  public void test_createTapOauthTokenInstanceForIncorrectToken_throwsException() throws Exception {
    new TapOauthToken("someincorrecttoken");
  }
}