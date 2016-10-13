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
package org.trustedanalytics.hadoop.config.client;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.SimpleInstanceConfiguration;

public class SimpleIstanceConfigurationTest
{
    private final String name = "test";
    private final String hdfsUri = "hdfs://nameservice1/tmp/%{organization}/brokers/userspace/efeedfce-05f5-4600-95f8-efd1f6c480f4/";
    private final Configuration hadoopConf;
    private ServiceInstanceConfiguration configuration;
    
    public SimpleIstanceConfigurationTest() {
        this.hadoopConf = new Configuration(false);
    }
    
    @Before
    public void before() {
        configuration = new SimpleInstanceConfiguration("test", hadoopConf, singletonMap(Property.HDFS_URI, hdfsUri));
    }
    
    @Test
    public void testGetName() {
        assertThat(configuration.getName(), equalTo(name));
    }
    
    @Test
    public void testGetProperty() {
        assertThat(configuration.getProperty(Property.HDFS_URI).get(), equalTo(hdfsUri));
    }
    
    @Test
    public void testGetConfiguration() {
        assertThat(configuration.asHadoopConfiguration(), equalTo(hadoopConf));
    }
}
