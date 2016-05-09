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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class Oauth2KrbCallbackHandlerTest {

  @Test(expected = NullPointerException.class)
  public void handleTest_callbacksArrayIsNull_throwsNLP() throws Exception {
    //given
    CallbackHandler toTest = new Oauth2KrbCallbackHandler();

    //when
    toTest.handle(null);

    //then throws NLP
  }

  @Test
  public void handleTest_callbacksArrayIsSize1_setOauth2TokenCallbackInstance() throws Exception {
    //given
    CallbackHandler toTest = new Oauth2KrbCallbackHandler();
    Callback[] callbacks = new Callback[1];

    //when
    toTest.handle(callbacks);

    //then
    Assert.assertNotNull(callbacks[0]);
    Assert.assertThat(callbacks[0], Matchers.instanceOf(Oauth2TokenCallback.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void handleTest_emptyCallbacksArray_throwsException() throws Exception {
    //given
    CallbackHandler toTest = new Oauth2KrbCallbackHandler();
    Callback[] callbacks = new Callback[]{};

    //when
    toTest.handle(callbacks);

    //then throws IllegalArgumentException
  }

  @Test
  public void handleTest_() throws Exception {
    //given
    CallbackHandler toTest = new Oauth2KrbCallbackHandler();
    Supplier<String> customTokenRetriver = () -> "";
    Callback[] callbacks = new Callback[]{new Oauth2TokenCallback(customTokenRetriver)};

    //when
    toTest.handle(callbacks);

    //then
    Assert.assertThat(((Oauth2TokenCallback)callbacks[0]).tokenRetriever(),
                      Matchers.is(customTokenRetriver));
  }
}