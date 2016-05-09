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

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ConfigOptionsTest {

  @Test
  public void asBooleanTest_existingOptionSetOnTrue_returnTrue() {
    Map<String, ?> options = ImmutableMap.of("useToken","true");
    boolean res = Oauth2KrbLoginModule.ConfigOptions.USE_TOKEN.asBoolean(options);
    assertTrue(res);
  }

  @Test
  public void asBooleanTest_notExistingOption_returnFalse() {
    Map<String, ?> options = ImmutableMap.of("useTokenCache","true");
    boolean res = Oauth2KrbLoginModule.ConfigOptions.TOKEN_CACHE.asBoolean(options);
    assertFalse(res);
  }

  @Test
  public void asString_existingOption_returnValueAsString() {
    Map<String, ?> options = ImmutableMap.of("ktinitCommand","/some/dir/ktinit_that_fails");
    Optional<String> res = Oauth2KrbLoginModule.ConfigOptions.KTINIT_COMMAND.asString(options);
    assertEquals("/some/dir/ktinit_that_fails", res.get());
  }

  @Test
  public void asString_notExistingOption_returnValueAsString() {
    Map<String, ?> options = ImmutableMap.of("ktinitCommand","/some/dir/ktinit_that_fails");
    Optional<String> res = Oauth2KrbLoginModule.ConfigOptions.TOKEN_CACHE.asString(options);
    assertFalse(res.isPresent());
  }
}