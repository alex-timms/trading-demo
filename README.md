# trading-demo
Matching engine takes in a ByteBuffer in FIX format, deserializes, validates and 
matches the order with appropriate bids/asks based on price-time priority, and rests
any unfulfilled orders.

## Project Structure
```text
trading-demo/
    aeron/          agents and infrastructure for Aeron messaging
    app/            creates the matching engine using the config
    common/         enums, exceptions, custom extensions, groupings
    config/         custom configuration
    engine/         matching engine
    events/         log trade events
    matcher/        price time matcher
    order/          order, order book definition
    serialization/  convert byteBuffer ◄─► order
    validation/     validate trade volume risk
```

## Project Flow Diagram
```text
Aeron "orders" -> MatchingEngine -> Deserialize -> Validate Order & Risk -> Matcher -> OrderBook
                                                                              |
                                                                              --> Aeron "events" Log
```

## Key Features
1. Send orders to the matching engine via Aeron.
2. Deserialize and validate incoming orders in FIX format.
3. Match orders based on price-time priority.
4. Emit trade events through an event sink (currently logged to console).

## Known limitations and planned work

**Error handling (planned, not yet implemented)**
- Orders that fail validation are currently dropped silently. Planned: emit a reject event with a reason.
- Malformed messages currently raise an exception in the consumer agent. Planned: route them through the same reject path.
- Duplicate order IDs are only detected when an order rests on the book, after matching. Planned: check before matching and reject.

**Scope**
- One instrument and limit orders only. The symbol (55) and order type (40) tags are not parsed, so non-limit orders are not rejected. Planned: multiple instruments and order-type validation.

**Other planned work**
- Publish events to Aeron (currently logged), an end-to-end test, broader unit test coverage, and a GitHub Actions workflow.

## Performance benchmarking

The matcher is benchmarked with [JMH](https://openjdk.org/projects/code-tools/jmh), the OpenJDK
microbenchmark harness. The aim is to measure what each candidate hot-path change (for example, removing
allocation) actually does to speed, bytes allocated per call and tail latency, one change at a time,
instead of assuming it helps.

- **What is measured:** `PriceTimeMatcher.match` against a populated order book, with a mix of resting
  orders, full fills and partial fills. The event sink is a no-op, so logging is excluded.
- **Status:** a baseline is recorded. The first change tried, removing the `Optional` chain in `peekBest`,
  made no measurable difference to speed or allocation.
- **Details:** how the benchmark works, how to run it, what each metric means and its target, and the
  full before/after results log are in
  [`src/test/java/ajt/bench/README.md`](src/test/java/ajt/bench/README.md).

New to JMH? It handles JIT warm-up, repeated runs in separate JVMs and allocation profiling, which
hand-written timing loops usually get wrong. The [official JMH page](https://openjdk.org/projects/code-tools/jmh)
links to the documentation and sample benchmarks.

## Assumptions
- The project currently assumes limit orders only (FIX `40=2`, `OrdType=Limit`).
- Other order types (for example market, IOC, FOK) are not supported yet.
- Built for only one instrument, need to expand for multiple
- Order IDs are assumed to be unique across the whole session. Duplicates are rejected by `DuplicateOrderIdValidator`, which keeps one set of every ID received. Multiple instruments will need this revisited (global versus per-instrument IDs).

## Build & Run

### Prerequisites
- Java 25
- Maven 3.9+

### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Run Benchmarks
See [`src/test/java/ajt/bench/README.md`](src/test/java/ajt/bench/README.md).

### Run Main (Maven)
```bash
mvn -Dexec.mainClass=ajt.Main -Dexec.jvmArgs="--add-opens java.base/sun.nio.ch=ALL-UNNAMED" exec:java
```

### Run Main (IntelliJ)
1. Open `ajt.Main`
2. Create or use an Application run configuration for `ajt.Main`
3. Add VM option: `--add-opens java.base/sun.nio.ch=ALL-UNNAMED`
4. Run

### Notes
- The app uses Aeron IPC (`aeron:ipc`) and starts an embedded MediaDriver.
- Orders are sent in FIX-like text format and consumed by `EngineConsumerAgent`.

