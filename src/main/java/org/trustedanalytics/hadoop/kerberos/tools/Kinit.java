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
package org.trustedanalytics.hadoop.kerberos.tools;

import com.beust.jcommander.JCommander;

import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import sun.security.krb5.KrbException;

import java.io.IOException;

import javax.security.auth.login.LoginException;

public class Kinit {

  public static void main(String[] args)
      throws LoginException, IOException, KrbException, InterruptedException {

    KinitParams params = new KinitParams();
    JCommander jc = new JCommander(params, args);
    if (params.isHelp()) {
      jc.usage();
      return;
    }

    KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
        .getKrbLoginManagerInstance(params.getKdc(), params.getRealm());
    loginManager.loginWithCredentials(params.getLogin(), params.getPassword());
  }
}