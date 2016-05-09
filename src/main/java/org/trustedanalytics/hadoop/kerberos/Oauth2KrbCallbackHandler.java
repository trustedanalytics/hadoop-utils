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

import java.io.IOException;
import java.util.function.Supplier;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class Oauth2KrbCallbackHandler implements CallbackHandler {

  private Supplier<String> tokenRetriverCode;

  private static final String TOKEN_CACHE_LOCATION = "OAUTH_TOKEN_LOCATION";

  public Oauth2KrbCallbackHandler() {
    this.tokenRetriverCode = new FromFileTokenRetriver(System.getenv(TOKEN_CACHE_LOCATION));
  }

  public Oauth2KrbCallbackHandler(Supplier<String> tokenRetriverCode) {
    this.tokenRetriverCode = tokenRetriverCode;
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException,
                                                  UnsupportedCallbackException {
    Preconditions.checkNotNull(callbacks);
    Preconditions.checkArgument(callbacks.length == 1,
                                "%s can't handle only one Callack",
                                Oauth2KrbCallbackHandler.class.getCanonicalName());
    if (callbacks[0] == null) {
      callbacks[0] = new Oauth2TokenCallback(this.tokenRetriverCode);
    }
  }

}