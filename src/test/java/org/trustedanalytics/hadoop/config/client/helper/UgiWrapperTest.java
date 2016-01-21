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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UgiWrapperTest {
  @Mock
  private UserGroupInformation ugi;

  private UgiWrapper testedObject;

  @Before
  public void setUp() throws Exception {
    testedObject = new UgiWrapper(ugi);
  }

  @Test
  public void doAs_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    String expected = "OK";
    UgiWrapper.YarnPrivilegedAction<String> lambda = () -> {
      return expected;
    };
    when(ugi.doAs(lambda)).thenReturn("OK");

    String result = testedObject.doAs(lambda);

    verify(ugi).doAs(lambda);
    assertThat(result, equalTo(result));
  }

  @Test(expected = IOException.class)
  public void doAs_lambdaPassedToFunction_invokeWithException() throws Exception {
    UgiWrapper.YarnPrivilegedAction<String> lambda = () -> {
      return null;
    };
    when(ugi.doAs(lambda)).thenThrow(new IOException());

    testedObject.doAs(lambda);
  }
}
