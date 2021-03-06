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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hawkular.metrics.christmasbox.model.Gauge;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author Joel Takvorian
 */
public class MemoryMonitoring implements MonitoringSession.FeederSet {

    private final Map<String, String> tags;

    public MemoryMonitoring(Map<String, String> tags) {
        this.tags = tags;
    }

    public MemoryMonitoring() {
        this(Collections.emptyMap());
    }

    @Override
    public Collection<MonitoringSession.Feeder> feeds(HawkularChristmasBox sessionBox) {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        List<MonitoringSession.Feeder> feeds = new ArrayList<>();
        Gauge sysFree = sessionBox.pickGauge("monitor.memory.system.free", tags);
        feeds.add(() -> sysFree.set(operatingSystemMXBean.getFreePhysicalMemorySize()));
        Gauge sysSwapFree = sessionBox.pickGauge("monitor.memory.system.swap.free", tags);
        feeds.add(() -> sysSwapFree.set(operatingSystemMXBean.getFreeSwapSpaceSize()));
        Gauge procHeap = sessionBox.pickGauge("monitor.memory.process.heap", tags);
        feeds.add(() -> procHeap.set(memoryUsage.getUsed()));
        return feeds;
    }
}
