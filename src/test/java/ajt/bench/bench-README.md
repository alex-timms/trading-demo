# Matcher benchmark

A JMH microbenchmark of `PriceTimeMatcher.match`. It exists to measure what each hot-path change makes to speed and allocation, one change at a time. See `hot-path-optimisation-notes.md` for the list of changes.

## What it tests

`MatcherBenchmark` runs the matcher against an `OrderBookImpl` with a no-op `EventSink`, so printing and formatting are not measured. Each benchmark call is a cycle of four `match` calls that leaves the book exactly as it found it:

| Call | Order | Effect |
|---|---|---|
| 1 | Rest a BUY, qty 100, at price P | Rests on the book |
| 2 | Rest a second BUY, qty 100, at price P | Rests behind the first |
| 3 | SELL aggressor, qty 150, at price P | Fully fills the first, partly fills the second |
| 4 | SELL aggressor, qty 50, at price P | Fills the rest of the second |

`bidSideCycle` runs this as written. `askSideCycle` is the mirror image (resting sells, buying aggressors).

Design points:
- **Steady state.** Every cycle removes what it adds, so the book never grows and the benchmark can run for any length of time.
- **Same input every run.** Prices P come from a pre-generated array filled with a seeded `Random(42)`, so every code change is measured against the identical order stream.
- **Background depth.** A set of resting orders sits far from the traded prices and never trades. It gives the `TreeMap`s realistic depth. `backgroundLevels` is a JMH parameter (100 and 1,000 levels, 10 orders per level), so each run produces two book-size scenarios.
- **Per-call results.** `@OperationsPerInvocation(4)` makes JMH divide the results by four, so every figure is per `match` call.
- **Allocation floor.** A new `Order` is allocated for every `match` call, because `leavesQty` is mutated. That is a constant part of the allocation figure in every run, so judge changes by how far the number moves, not by whether it reaches zero.

### Settings

5 warm-up and 8 measurement iterations of 2 seconds each, in 2 forks, measuring both average time and sampled time. Each one addresses a specific problem with timing Java code:

| Setting | What it does | Why |
|---|---|---|
| Warm-up: 5 × 2 s | Runs the benchmark for 10 seconds before recording anything. Those results are discarded. | The JVM starts by interpreting bytecode, and the JIT compiler gradually compiles and optimises the hot code (inlining, removing allocations). Timings taken during that are slow and unstable. Warm-up means you measure steady-state speed, as in a long-running process. |
| Measurement: 8 × 2 s | Runs 8 recorded iterations, each repeating the method as often as it can for 2 seconds. | Each iteration gives its own score. JMH reports the mean and a confidence interval (the `±`), which shows how stable the result is. `29.884 ± 1.768 ns/op` means the true average is very likely between about 28.1 and 31.7 ns. The `±` is what lets you tell a real change from noise. |
| 2 forks | Runs the whole warm-up and measurement in 2 separate fresh JVMs and combines the results. | One JVM run can be unrepresentative, because JIT decisions and heap layout vary slightly between runs. Separate JVMs average that out. It is also why `Cnt` is 16 in the output (2 forks × 8 iterations), and why identical code gives slightly different numbers each time. |
| Average time (`avgt`) | Repeats the method for 2 seconds and divides total time by the number of calls. | Low overhead, so the most accurate average. Use it to compare changes. |
| Sample time (`sample`) | Times individual calls and keeps the timings. | Gives percentiles (p50, p99, p99.9 and so on), so you can see the tail. The timer adds overhead to every call, so sample scores (35 to 61 ns in the baseline) are higher than `avgt` scores (28 to 41 ns). Use it for tail latency, not for the average. |

Trade-off: more iterations and forks give tighter error bars but a longer run. The full settings across 2 book sizes, 2 methods and 2 modes take about five minutes. For a quick check while developing, add `-f 1 -wi 3 -i 3` to the command, and use the full settings for anything you record in the results log.

## What it does not test

- Event publishing, logging and `println` (the sink is a no-op).
- Aeron, deserialisation and the idle strategy. These need an end-to-end harness.
- A multi-threaded or multi-instrument workload.

## How to run

1. Test dependencies in `pom.xml` (scope `test`): `org.openjdk.jmh:jmh-core` and `org.openjdk.jmh:jmh-generator-annprocess`, version 1.37.
2. On JDK 23 and later, `javac` does not run annotation processors found on the classpath unless asked. Without this, the run fails with `Unable to find the resource: /META-INF/BenchmarkList`. Declare the processor on the compiler plugin:

   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
           <annotationProcessorPaths>
               <path>
                   <groupId>org.openjdk.jmh</groupId>
                   <artifactId>jmh-generator-annprocess</artifactId>
                   <version>1.37</version>
               </path>
           </annotationProcessorPaths>
       </configuration>
   </plugin>
   ```

3. Build and run:

   ```bash
   mvn clean test-compile
   mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
   java -cp target/test-classes:target/classes:$(cat cp.txt) \
        org.openjdk.jmh.Main MatcherBenchmark -prof gc
   ```

   The full run takes about five minutes. For a quick smoke test, add `-f 1 -wi 3 -i 3`. On Windows use `;` as the classpath separator, or run from Git Bash.

4. Close other heavy programs while it runs, and compare runs made on the same machine.

## Key metrics and targets

Read the rows with `Mode = avgt` for time and allocation, and `Mode = sample` for percentiles. Ignore the `gc.alloc.rate`, `gc.count` and `gc.time` rows when judging a change: they depend on how fast the code runs and move as a result of the other figures.

| Metric (row name) | What it is | Target |
|---|---|---|
| `…Cycle` score, `avgt` (ns/op) | Average time per `match` call | **Down.** Changes smaller than the ± error (about 2 ns) are noise: record them as "no measurable change". |
| `…:gc.alloc.rate.norm` (B/op) | Bytes allocated per `match` call. The most reliable figure, because it is deterministic. | **Down.** Expect it to approach the `Order` allocation (about 48 B), not zero. |
| `…:p0.999`, `…:p0.9999` (ns/op) | Latency that 99.9% and 99.99% of calls stay under (`sample` rows) | **Down.** This is the tail. Trust only a clear drop that repeats across runs. |

Notes:
- The average, B/op and the percentiles are reported separately for each `backgroundLevels` value (100 and 1,000). Compare like with like.
- `p1.00` (the slowest single sample) is dominated by one-off pauses. Do not base conclusions on it.
- A change can lower B/op without moving ns/op. That is a valid result: less garbage, no measurable speed change in this benchmark.

## Baseline

Before any hot-path change. JDK as built locally. `avgt` rows and `sample` percentile rows from the same run.

| Metric | `backgroundLevels` = 100 | `backgroundLevels` = 1000 |
|---|---|---|
| `askSideCycle` ns/op (avgt) | 29.884 ± 1.768 | 41.098 ± 1.697 |
| `bidSideCycle` ns/op (avgt) | 28.441 ± 1.599 | 40.515 ± 2.214 |
| B/op (`gc.alloc.rate.norm`) | 162 | 162 |
| `askSideCycle` p0.999 (ns/op) | 165.4 | 250.0 |
| `bidSideCycle` p0.999 (ns/op) | 114.0 | 1085.6 |
| `askSideCycle` p0.9999 (ns/op) | 2281.9 | 2120.3 |
| `bidSideCycle` p0.9999 (ns/op) | 1760.0 | 24262.9 |

## Results log

Add one row per change, measured with the same command and on the same machine. Record the `askSideCycle` and `bidSideCycle` figures for the 1,000-level scenario, or both if you have room.
Noise level: repeat runs of unchanged code differ by about 1 to 3 ns/op, so treat smaller differences as "no measurable change". The tail percentiles (p0.999, p0.9999) vary a lot between runs of the same code; only trust a drop that repeats.

| Change | ns/op (ask, 1000) | ns/op (bid, 1000) | B/op | p0.999 (ask, 1000) | p0.9999 (ask, 1000) | Verdict |
|---|---|---|---|---|---|---|
| Baseline | 41.098 | 40.515 | 162 | 250.0 | 2120.3 | |
| Baseline (rerun) | 39.596 | 39.501 | 162 | 406.0 | 11068.9 | Reproduces baseline within noise |
| `peekBest` via `firstKey` + `get` | 44.471 | 52.851 | 162 | 156.0 | 1686.0 | Slower by 5 to 13 ns: `get` repeats the tree search that `firstKey` already did. Reverted. |
| `peekBest` single descent (`firstEntry` + `peekFirst`) | 36.669 | 41.900 | 162 | 322.0 | 4278.0 | No measurable change. Kept: same speed, simpler, does not rely on escape analysis. |
| Primitive locals in `DeserializeOrder`, `DirectBuffer` input | | | | | | Not covered by this benchmark (it never calls the deserialiser) |
| `Long2ObjectHashMap` for the ID index | | | | | | |
| `removeFirst()` instead of `remove(Object)` | | | | | | |

### Notes

- Removing the `Optional` chain from `peekBest` changed neither time nor allocation (162 B/op throughout). The JIT's escape analysis was most likely already removing those objects, so the real allocation is elsewhere: probably boxed `Long` keys, `HashMap` nodes, and a new `Orders` deque for each price level.
- `Order` itself accounts for roughly 48 B of the 162 B/op and cannot be removed in this benchmark.
- Raw per-run output for each row above is the JMH summary table; keep it alongside the commit so a figure can be re-checked.

## Reading the results with care

- The baseline tail figures come from a single run of two forks. A large p0.9999 or p1.00 may be a one-off pause, so rerun before reading anything into it.
- If you change `STEP`, `TRADED_LEVELS`, `BID_MIN` or `ASK_MIN`, keep the traded bid range below the traded ask range, and keep the background bids below the lowest traded bid, so the cycle's orders only ever match each other.
- Only quote a performance figure (for example on a CV) once you can reproduce it with the commands above.
