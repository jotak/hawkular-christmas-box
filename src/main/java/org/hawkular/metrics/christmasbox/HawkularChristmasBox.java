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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hawkular.metrics.christmasbox.model.Counter;
import org.hawkular.metrics.christmasbox.model.CounterChangeListener;
import org.hawkular.metrics.christmasbox.model.Gauge;
import org.hawkular.metrics.christmasbox.model.GaugeChangeListener;
import org.hawkular.metrics.christmasbox.model.Watch;


public class HawkularChristmasBox implements CounterChangeListener, GaugeChangeListener {

    private static final String WARNING_PREFIX = "warning/";
    private static final String ERROR_PREFIX = "error/";
    private static final String TOTAL_SUFFIX = "@total";

    private final Optional<String> prefix;
    private final HawkularHttpClient hawkularClient;
    private final MetricsTagger metricsTagger;
    private final Map<String, Counter> counters = new HashMap<>();
    private final Map<String, Gauge> gauges = new HashMap<>();
    private final Map<String, Watch> watches = new HashMap<>();

    HawkularChristmasBox(HawkularHttpClient hawkularClient,
                         Optional<String> prefix,
                         Map<String, String> globalTags,
                         Map<String, Map<String, String>> perMetricTags,
                         Collection<RegexTags> regexTags,
                         boolean enableAutoTagging) {
        this.prefix = prefix;
        this.hawkularClient = hawkularClient;
        metricsTagger = new MetricsTagger(prefix, globalTags, perMetricTags, regexTags, enableAutoTagging,
                hawkularClient);
    }

    public Gauge pickGauge(String name) {
        return pickGauge(name, Collections.emptyMap());
    }

    public Gauge pickGauge(String name, Map<String, String> tags) {
        String fullname = prefix.map(p -> p + name).orElse(name);
        if (gauges.containsKey(fullname)) {
            return gauges.get(fullname);
        }
        Gauge g = new Gauge(fullname, this);
        gauges.put(fullname, g);
        metricsTagger.registerMetricTags(name, tags);
        metricsTagger.onGaugeAdded(name);
        return g;
    }

    public Counter pickCounter(String name) {
        return pickCounter(name, Collections.emptyMap());
    }

    public Counter pickCounter(String name, Map<String, String> tags) {
        String fullname = prefix.map(p -> p + name).orElse(name);
        if (counters.containsKey(fullname)) {
            return counters.get(fullname);
        }
        Counter c = new Counter(fullname, this);
        counters.put(fullname, c);
        metricsTagger.registerMetricTags(name, tags);
        metricsTagger.onCounterAdded(name);
        return c;
    }

    public Watch pickWatch(String name) {
        return pickWatch(name, Collections.emptyMap());
    }

    public Watch pickWatch(String name, Map<String, String> tags) {
        String fullname = prefix.map(p -> p + name).orElse(name);
        if (watches.containsKey(fullname)) {
            return watches.get(fullname);
        }
        Watch w = new Watch(fullname, this);
        watches.put(fullname, w);
        metricsTagger.registerMetricTags(name, tags);
        metricsTagger.onGaugeAdded(name);
        return w;
    }

    public void ohOhShit(Class<?> guilty) {
        ohOhShit(guilty.getName());
    }

    public void ohOhShit(String guilty) {
        pickCounter(WARNING_PREFIX + guilty, Collections.singletonMap("type", "warning")).inc();
        pickCounter(WARNING_PREFIX + TOTAL_SUFFIX, Collections.singletonMap("type", "warning")).inc();
    }

    public void ohOhFuck(Class<?> guilty) {
        ohOhFuck(guilty.getName());
    }

    public void ohOhFuck(String guilty) {
        pickCounter(ERROR_PREFIX + guilty, Collections.singletonMap("type", "error")).inc();
        pickCounter(ERROR_PREFIX + TOTAL_SUFFIX, Collections.singletonMap("type", "error")).inc();
    }

    public MonitoringSession.Builder prepareMonitoringSession(long frequency, TimeUnit timeUnit) {
        return new MonitoringSession.Builder(frequency, timeUnit);
    }

    /**
     * Create a new builder for an {@link HawkularChristmasBox}
     * @param tenant   the Hawkular tenant ID
     */
    public static WishList startWishList(String tenant) {
        return new WishList(tenant);
    }

    // TODO: allow batch changes
    @Override public void onChanged(Gauge g) {
        Long timestamp = System.currentTimeMillis();
        hawkularClient.postMetrics(HawkularJson.metricsToString(
                timestamp,
                Collections.emptyMap(),
                Collections.singletonMap(g.getName(), g.getValue())));
    }

    @Override public void onChanged(Counter c) {
        Long timestamp = System.currentTimeMillis();
        hawkularClient.postMetrics(HawkularJson.metricsToString(
                timestamp,
                Collections.singletonMap(c.getName(), c.getCount()),
                Collections.emptyMap()));
    }
}
