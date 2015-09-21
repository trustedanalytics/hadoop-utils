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

import com.beust.jcommander.Parameter;

public class KinitParams {

  @Parameter(names = {"-realm", "-r"}, description = "Default realm", required = true)
  private String realm;

  @Parameter(names = "-kdc", description = "KDC adress", required = true)
  private String kdc;

  @Parameter(names = {"-princ", "-login", "-user", "-u"}, description = "User name", required = true)
  private String login;

  @Parameter(names = {"-p", "-pass", "-password"}, description = "Password", password = true)
  private String password;

  @Parameter(names = {"-h", "-?", "-help"}, help = true)
  private boolean help;

  public String getRealm() {
    return realm;
  }

  public String getKdc() {
    return kdc;
  }

  public String getLogin() {
    return login;
  }

  public char[] getPassword() {
    return password.toCharArray();
  }

  public boolean isHelp() {
    return help;
  }
}
