/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package org.hawkular.metrics.christmasbox;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Joel Takvorian
 */
class MetricsTagger {

    private static final String METRIC_TYPE_COUNTER = "counters";
    private static final String METRIC_TYPE_GAUGE = "gauges";

    private final Optional<String> prefix;
    private final Map<String, String> globalTags;
    private final Map<String, Map<String, String>> perMetricTags;
    private final Collection<RegexTags> regexTags;
    private final boolean enableAutoTagging;
    private final HawkularHttpClient hawkularClient;

    MetricsTagger(Optional<String> prefix, Map<String, String> globalTags, Map<String, Map<String, String>>
            perMetricTags, Collection<RegexTags> regexTags, boolean enableAutoTagging,
                  HawkularHttpClient hawkularClient) {
        this.prefix = prefix;
        this.globalTags = globalTags;
        this.perMetricTags = perMetricTags;
        this.regexTags = regexTags;
        this.enableAutoTagging = enableAutoTagging;
        this.hawkularClient = hawkularClient;
    }

    public void registerMetricTags(String metricName, Map<String, String> tags) {
        Map<String, String> existingTags = perMetricTags.computeIfAbsent(metricName, k -> new HashMap<>());
        existingTags.putAll(tags);
    }

    private void tagMetric(String metricType, String baseName) {
        String fullName = prefix.map(p -> p + baseName).orElse(baseName);
        Map<String, String> tags = new LinkedHashMap<>(globalTags);
        // Don't use prefixed name for per-metric tagging
        tags.putAll(getTagsForMetrics(baseName));
        if (!tags.isEmpty()) {
            try {
                String encodedFullName = URLEncoder.encode(fullName, "UTF-8");
                hawkularClient.putTags("/" + metricType
                        + "/" + encodedFullName + "/tags", HawkularJson.tagsToString(tags));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, String> getTagsForMetrics(String name) {
        Map<String, String> tags = new LinkedHashMap<>();
        regexTags.forEach(reg -> reg.match(name).ifPresent(tags::putAll));
        if (perMetricTags.containsKey(name)) {
            tags.putAll(perMetricTags.get(name));
        }
        return tags;
    }

    void onGaugeAdded(String name) {
        tagMetric(METRIC_TYPE_GAUGE, name);
    }

    void onCounterAdded(String name) {
        tagMetric(METRIC_TYPE_COUNTER, name);
    }

    Map<String, String> getGlobalTags() {
        return globalTags;
    }

    boolean isEnableAutoTagging() {
        return enableAutoTagging;
    }

}
