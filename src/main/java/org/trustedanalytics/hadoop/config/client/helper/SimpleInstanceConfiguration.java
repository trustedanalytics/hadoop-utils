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

import java.util.Optional;
import org.trustedanalytics.hadoop.config.client.Property;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;

public class SimpleInstanceConfiguration implements ServiceInstanceConfiguration
{
    private final String name;
    private final Configuration configuration;
    private final Map<Property, String> properties;
    
    public SimpleInstanceConfiguration(final String name, final Configuration configuration, final Map<Property, String> properties) {
        this.name = name;
        this.configuration = configuration;
        this.properties = properties;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Configuration asHadoopConfiguration() {
        return this.configuration;
    }
    
    public Map<String, String> asMap() {
        return null;
    }
    
    public Optional<String> getProperty(final Property propertyLocation) throws IllegalStateException {
        return Optional.of(this.properties.get(propertyLocation));
    }
}
