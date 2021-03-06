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
import java.lang.management.RuntimeMXBean;
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
public class CPUMonitoring implements MonitoringSession.FeederSet {

    private static final HawkularChristmasBox HWK = HawkularChristmasFactory.get(CPUMonitoring.class);

    private final Map<String, String> tags;

    public CPUMonitoring(Map<String, String> tags) {
        this.tags = tags;
    }

    public CPUMonitoring() {
        this(Collections.emptyMap());
    }

    @Override
    public Collection<MonitoringSession.Feeder> feeds(HawkularChristmasBox sessionBox) {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<MonitoringSession.Feeder> feeds = new ArrayList<>();
        Gauge coreCpuGauge = sessionBox.pickGauge("monitor.cpu.core", tags);
        Gauge globalCpuGauge = sessionBox.pickGauge("monitor.cpu.global", tags);
        feeds.add(() -> coreCpuGauge.set(reportCPU(operatingSystemMXBean, runtimeMXBean, false)));
        feeds.add(() -> globalCpuGauge.set(reportCPU(operatingSystemMXBean, runtimeMXBean, true)));
        return feeds;
    }

    private static double reportCPU(OperatingSystemMXBean operatingSystemMXBean,
                                   RuntimeMXBean runtimeMXBean,
                                   boolean divideByAvailableProcs) {
        int availableProcessors = divideByAvailableProcs ? operatingSystemMXBean.getAvailableProcessors() : 1;
        long prevUpTime = runtimeMXBean.getUptime();
        long prevProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            HWK.ohOhFuck(e.getClass());
        }

        operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long upTime = runtimeMXBean.getUptime();
        long processCpuTime = operatingSystemMXBean.getProcessCpuTime();
        long elapsedCpu = processCpuTime - prevProcessCpuTime;
        long elapsedTime = upTime - prevUpTime;

        return Math.min(99F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
    }
}
