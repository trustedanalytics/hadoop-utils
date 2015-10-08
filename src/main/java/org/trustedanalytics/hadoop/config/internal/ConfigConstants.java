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
package org.trustedanalytics.hadoop.config.internal;

public final class ConfigConstants {

  /**
   * Environment variable name set by cloud foundry. Services params bound to application.
   */
  public static final String VCAP_SERVICES = "VCAP_SERVICES";

  /**
   * JsonNode name where are configuration parameters for hadoop.
   */
  public static final String HADOOP_CONFIG_KEY_VALUE = "HADOOP_CONFIG_KEY";

  public static final String KRB_CONF_NODE_NAME = "kerberos";

  public static final String KDC_ADRESS_PROP_NAME = "kdc";

  public static final String REALM_NAME_PROP_NAME = "krealm";

  public static final String HDFS_URI = "uri";

  public static final String ZOOKEEPER_CLUSTER = "zk.cluster";

  public static final String ZOOKEEPER_ZNODE = "zk.node";

  public static final String HDFS_SERVICE_TYPE_NAME = "hdfs";

  public static final String YARN_SERVICE_TYPE_NAME = "yarn";

  public static final String HBASE_SERVICE_TYPE_NAME = "hbase";

  public static final String GEAR_PUMP_SERVICE_TYPE_NAME = "gearpump";

  public static final String ZOOKEEPER_SERVICE_TYPE_NAME = "zookeeper";

  public static final String CREDENTIALS = "credentials";

  public static final String USER_PROVIDED = "user-provided";

  public static final String USER_PROP_NAME = "kuser";

  public static final String PASSWORD_PROP_NAME = "kpassword";

  public static final String HBASE_PROP_NAME_NAMESPACE = "hbase.namespace";

  public static final String INSTANCE_NAME_PROP_NAME = "name";

  private ConfigConstants() { }

}