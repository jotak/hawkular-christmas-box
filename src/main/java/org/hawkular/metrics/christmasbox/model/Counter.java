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
package org.hawkular.metrics.christmasbox.model;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Joel Takvorian
 */
public class Counter extends Metric {
    private LongAdder count = new LongAdder();
    private final CounterChangeListener listener;

    public Counter(String name, CounterChangeListener listener) {
        super(name);
        this.listener = listener;
    }

    public void inc() {
        count.increment();
        listener.onChanged(this);
    }

    public Long getCount() {
        return count.longValue();
    }
}
