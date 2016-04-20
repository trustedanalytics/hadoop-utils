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

import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.exceptions.YarnException;

import com.google.common.base.Throwables;

public class UgiWrapper {

  private final UserGroupInformation ugi;

  public UgiWrapper(UserGroupInformation ugi) {
    this.ugi = ugi;
  }

  public <T> T doAs(YarnPrivilegedAction<T> func) throws IOException {
    try {
      return ugi.doAs(func);
    } catch (InterruptedException impossible) {
      Thread.currentThread().interrupt();
      throw Throwables.propagate(impossible);
    }
  }

  public interface YarnPrivilegedAction<T> extends PrivilegedExceptionAction<T> {
    @Override
    T run() throws YarnException, IOException;
  }
}
