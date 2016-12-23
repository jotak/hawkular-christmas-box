# hawkular-christmas-box

**Merry Christmas Hawkular!** See what Santa has in his sack...

![Logo](/docs/img/hawkmas.png)

Integrate metrics in your app just like you would use a Logger; and don't bother about sending data to Hawkular, Santa will do it for you.

````java
private static final HawkularChristmasBox HWK = HawkularChristmasFactory.get(MyCoolApp.class);
````

Or more accurately build your wishlist:

````java
HawkularChristmasBox hawkular =  HawkularChristmasBox.startWishList("tenant")
        .addGlobalTag("owner", owner)
        .prefixedWith(owner + ".")
        .sendToSanta();
````

You can of course have your own applicative metrics:

````java
Gauge gauge = HWK.pickGauge("my.gauge");
Counter counter = HWK.pickCounter("my.counter");

// Later on...
gauge.set(val);
counter.inc();
````

But there's more. A new type of metric has appeared and can be very helpful for debugging: the WATCH. It's actually just a gauge with a special disguise.

````java
Watch watch = HWK.pickWatch("my.watch");

// Later on...
while (running) {
  // Doing something...
  watch.tick();
}
````

What else? I started with the analogy of a Logger. And indeed, you can "log" errors, for instance to count occurences of some exceptions.

````java
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

In Hawkular / Grafana you can convert Counters to rates, so why not setting an alert when errors rate get too high?

Is that all? No! The best for the end: start monitoring sessions for any specific piece of code! For instance if you want to monitor just a specific algorithm and stop when it's over.

````java
MonitoringSession monitoringSession = HWK.prepareMonitoringSession(500, TimeUnit.MILLISECONDS)
                .feeds(CPUMonitoring.feeds(HWK))
                .feeds(MemoryMonitoring.feeds(HWK))
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


PS: is it a joke or not? Partly. It could be turned into a kind of "Hawkular Toolbox", though I'm not sure if there's a public for that. There's definitely some DropWizard flavour here, and nothing we cannot do with existing libs. But if the goal is be attractive to the developers, being as user-friendly as possible and considering their use cases, being responsive to feature requests etc., maybe there's a card to play.
