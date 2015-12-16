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

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;

public final class TapOauthToken implements JwtToken {

  private final String rawToken;

  private String userName;

  public TapOauthToken(String token) {
    rawToken = token;
    try {
      JwtConsumer consumer = new JwtConsumerBuilder().setSkipAllValidators()
          .setDisableRequireSignature()
          .setSkipSignatureVerification().build();

      JwtContext context = consumer.process(rawToken);
      userName = (String) context.getJwtClaims().getClaimsMap().get("user_name");
    } catch (InvalidJwtException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getUserName() {
    return userName;
  }

  @Override
  public String getRawToken() {
    return rawToken;
  }
}
