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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.hawkular.metrics.christmasbox.model.Counter;
import org.hawkular.metrics.christmasbox.model.Gauge;
import org.hawkular.metrics.christmasbox.model.Watch;

/**
 * @author Joel Takvorian
 */
public class HawkularChristmasBoxTest {

    private static final List<Integer> INSECURE_PRIME_NUMBERS_LIST = new ArrayList<>();
    private static final HawkularChristmasBox CHRISTMAS_BOX = HawkularChristmasFactory.get("MyCoolApp");

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(() -> run(1, HawkularChristmasBoxTest::isPrime1));
        executor.execute(() -> run(2, HawkularChristmasBoxTest::isPrime2));
        executor.execute(() -> run(3, HawkularChristmasBoxTest::isPrime3));

        try {
            Thread.sleep(60*1000);
        } catch (InterruptedException e) {
            CHRISTMAS_BOX.ohOhFuck(e.getClass());
        }
        MonitoringSession monitoringSession = CHRISTMAS_BOX.prepareMonitoringSession(500, TimeUnit.MILLISECONDS)
                        .feeds(new CPUMonitoring(Collections.singletonMap("type", "cpu")))
                        .feeds(new MemoryMonitoring(Collections.singletonMap("type", "memory")))
                        .start();
        try {
            Thread.sleep(60*1000);
        } catch (InterruptedException e) {
            CHRISTMAS_BOX.ohOhFuck(e.getClass());
        }
        executor.shutdownNow();
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            CHRISTMAS_BOX.ohOhFuck(e.getClass());
        }
        monitoringSession.stop();
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            CHRISTMAS_BOX.ohOhFuck(e.getClass());
        }
        System.out.println("List size: " + INSECURE_PRIME_NUMBERS_LIST.size());
    }

    private static void run(int method, Predicate<Integer> isPrime) {
        Gauge primes = CHRISTMAS_BOX.pickGauge("primes.value." + method, Collections.singletonMap("type", "value"));
        Counter counter = CHRISTMAS_BOX.pickCounter("primes.count." + method, Collections.singletonMap("type", "counter"));
        Watch watch = CHRISTMAS_BOX.pickWatch("primes.watch." + method, Collections.singletonMap("type", "watch"));
        int i = 0;
        while (true) {
            try {
                if (isPrime.test(i)) {
                    // Dammit! But... it's crap!
                    Iterator<Integer> it = INSECURE_PRIME_NUMBERS_LIST.iterator();
                    while (it.hasNext()) {
                        Integer other = it.next();
                        if (other.equals(i)) {
                            it.remove();
                        }
                    }
                    INSECURE_PRIME_NUMBERS_LIST.add(i);
                    counter.inc();
                    watch.tick();
                    primes.set((double) i);
                }
                i++;
            } catch (Throwable t) {
                CHRISTMAS_BOX.ohOhFuck(t.getClass());
                t.printStackTrace();
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                CHRISTMAS_BOX.ohOhShit(e.getClass());
                break;
            }
        }
    }

    private static boolean isPrime1(int n) {
        for(int i=2;i<n;i++) {
            if(n%i==0)
                return false;
        }
        return true;
    }

    private static boolean isPrime2(int n) {
        for(int i=2;2*i<n;i++) {
            if(n%i==0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPrime3(int n) {
        //check if n is a multiple of 2
        if (n%2==0) return false;
        //if not, then just check the odds
        for(int i=3;i*i<=n;i+=2) {
            if(n%i==0)
                return false;
        }
        return true;
    }
}
