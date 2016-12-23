# hawkular-christmas-box
Merry Christmas Hawkular!

![Logo](/docs/img/hawkmas.png)

See what Santa has in his sack:

Integrate metrics in your app just like you would use a Logger; and don't bother about sending data to Hawkular, Santa will do it for you.

````java
    private static final HawkularChristmasBox HWK = HawkularChristmasFactory.get(MyCoolApp.class);

    // Later on...
    // Something bad happened?
    HWK.ohOhShit("my-bad-thing");   // That will translate into a warning

    // Something really bad? Like OOM?
    HWK.ohOhFuck(ex.getClass());    // That will translate into an error
````

And then, you get:
![Warnings](/docs/img/Warnings.png)
_Warnings_

![Errors](/docs/img/Errors.png)
_Errors_

What else? You can of course have your own applicative metrics:

````java
    Gauge gauge = HWK.pickGauge("my.gauge");
    Counter counter = HWK.pickCounter("my.counter");

    // Later on...
    gauge.set(val);
    counter.inc();
````

But there's more. A new metric has appeared and can be very helpful for debugging: the WATCH. It's actually just a gauge with a special disguise.

````java
    Watch watch = HWK.pickWatch("my.watch");

    // Later on...
    while (running) {
      // Doing something...
      watch.tick();
    }
````

Is that all? No! The best for the end: start monitoring sessions for any specific piece of code! For instance if you want to monitor just a specific algorithm and stop when it's over.

````java
    MonitoringSession monitoringSession = HWK.prepareMonitoringSession(500, TimeUnit.MILLISECONDS)
                    .feeds(CPUMonitoring.feeds(HWK, Collections.singletonMap("type", "cpu")))
                    .feeds(MemoryMonitoring.feeds(HWK, Collections.singletonMap("type", "memory")))
                    .start();

    // Run the algorithm

    monitoringSession.stop();
````

And then, you get:
![CPU](/docs/img/CPU.png)
_CPU_

![Memory](/docs/img/Memory.png)
_Memory_

See the big picture:
![Big picture](/docs/img/All.png)

[Here's some sample code](src/test/java/org/hawkular/metrics/christmasbox/HawkularChristmasBoxTest.java)


(PS: it's being presented like a joke - but why not turning it into a real Hawkular Toolbox with some polishing)
