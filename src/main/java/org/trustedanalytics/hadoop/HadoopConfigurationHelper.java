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
package org.trustedanalytics.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.ConfigConstants;

public final class HadoopConfigurationHelper {


    private static final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Environment variable name set by cloud foundry. Services params bound to application.
     */
    public static final String VCAP_SERVICES = "VCAP_SERVICES";

    /**
     * Merge additional parameters to hadoop configuration. This method overwrites parameters in
     * conf.
     *
     * @param conf hadoop configuration
     * @param props parameters to merge
     */
    public static void mergeConfiguration(Configuration conf, Map<String, String> props) {
        if (props != null) {
          props.forEach(conf::set);
        }
    }

    /**
     * Default hidden constructor for class
     */
    private HadoopConfigurationHelper() {

    }

    /**
     * Search hadoop configuration parameters in environment.
     *
     * @param envVariableName environment variable name with configuration
     * @return map of configuration properties
     */
    public static Optional<Map<String, String>> getHadoopConfFromEnv(String envVariableName)
            throws IOException {
        return getHadoopConfFromJson(System.getenv(envVariableName));
    }

    /**
     * Search all hadoop configuration parameters.
     *
     * @param jsonConf string with configuration
     * @return map of configuration properties
     * @throws java.lang.IllegalArgumentException if jsonConf is null
     * @throws IOException
     */
    public static Optional<Map<String, String>> getHadoopConfFromJson(String jsonConf)
            throws IOException, IllegalArgumentException {
        if (jsonConf == null) {
          throw new IllegalArgumentException("Json configuration string can not be null!");
        }
        // start searching from root node down.
        JsonNode jsonNode = getConfigurationAsJsonTree(jsonConf);
        return Optional.of(parseJsonConf(jsonNode));
    }

    public static Optional<Map<String, String>> getHadoopConfByServiceName(String jsonConf,
            HadoopServiceType type) throws IOException {
        return getHadoopConfByServiceName(jsonConf, type.getTypeName());
    }

    /**
     * Search hadoop configuration parameters for only one service type.
     *
     * @param jsonConf string with configuration
     * @param type service type
     * @return map of configuration properties
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static Optional<Map<String, String>> getHadoopConfByServiceName(String jsonConf,
            String type) throws IOException {
        // start searching from service type node down.
        JsonNode jsonNode = getConfigurationAsJsonTree(jsonConf).findParent(type);
        return Optional.of(parse(jsonNode));
    }

    private static Map<String, String> parse(JsonNode jsonNode) {
        Map<String, String> found = new HashMap<>();
        JsonNode hadoopConfParent = jsonNode.findParent(ConfigConstants.HADOOP_CONFIG_KEY_VALUE);
        Iterator<JsonNode> trustedAnalyticsConfSections = hadoopConfParent.getElements();
        trustedAnalyticsConfSections.forEachRemaining(entry -> {
            Iterator<Map.Entry<String, JsonNode>> paramRowfields = entry.getFields();
            paramRowfields.forEachRemaining(paramEntry -> found.put(paramEntry.getKey(),
                    paramEntry.getValue().getTextValue()));
        });
        return found;
    }

    private static Map<String, String> parseJsonConf(JsonNode jsonNode) throws IOException {
        Map<String, String> found = new HashMap<>();
        List<JsonNode> hadoopConfParents = jsonNode.findParents(ConfigConstants.HADOOP_CONFIG_KEY_VALUE);
        for (JsonNode hadoopConf : hadoopConfParents) {
            found.putAll(parse(hadoopConf));
        }
        return found;
    }

    private static JsonNode getConfigurationAsJsonTree(String jsonConf) throws IOException {
        return jsonMapper.readTree(jsonConf);
    }
}
