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

import org.junit.Test;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import static org.junit.Assert.assertArrayEquals;

public class FixedPasswordHandlerTest {

  private static String PASSWORD = "haselko_maselko";

  @Test(expected = UnsupportedCallbackException.class)
  public void testHandle_getUnsupportedCallbackType_throwException() throws Exception {
    HadoopKrbLoginManager.FixedPasswordHandler handler =
        new HadoopKrbLoginManager.FixedPasswordHandler(PASSWORD.toCharArray());
    Callback someUnsupportedCallback = new Callback() {};
    Callback[] callbacks = new Callback[] {someUnsupportedCallback};

    handler.handle(callbacks);
  }

  @Test
  public void testHandle_getPasswordCallbackType_setPasswordInCallbackObject() throws Exception {
    HadoopKrbLoginManager.FixedPasswordHandler handler =
        new HadoopKrbLoginManager.FixedPasswordHandler(PASSWORD.toCharArray());
    Callback callback = new PasswordCallback("input password", true);
    Callback[] callbacks = new Callback[] {callback};

    handler.handle(callbacks);
    assertArrayEquals(PASSWORD.toCharArray(), ((PasswordCallback) callback).getPassword());
  }
}