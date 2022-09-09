Benchmarks for Togglz
======

Running the benchmarks
---

To run one of the benchmarks in your IDE, each one has a `main` method on it that will execute that single benchmark.

To run them on a remote server, or outside of your IDE, you can package up an uber-jar that contains all of the necessary
files to run the tests in one jar by running `mvn clean package`. The uber-jar is located in `target/togglz-benchmarks-uberjar.jar`.

You can then run the benchmarks by executing `java -jar target/togglz-benchmarks-uberjar.jar`


About JMH
---
The benchmarks here use the ["Java Micro-benchmark Harness" (aka JMH)](http://openjdk.java.net/projects/code-tools/jmh/)
- widely considered to be the "gold standard" for implementing microbenchmarks on the JVM.

You can start to learn more about how JMH works by [reading through their sample tests.](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
