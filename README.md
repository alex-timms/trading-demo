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
4. Publish events to Aeron, currently just logged

## Known limitations and planned work

**Error handling (planned, not yet implemented)**
- Orders that fail validation are currently dropped silently. Planned: emit a reject event with a reason.
- Malformed messages currently raise an exception in the consumer agent. Planned: route them through the same reject path.
- Duplicate order IDs are only detected when an order rests on the book, after matching. Planned: check before matching and reject.

**Scope**
- One instrument and limit orders only. The symbol (55) and order type (40) tags are not parsed, so non-limit orders are not rejected. Planned: multiple instruments and order-type validation.

**Other planned work**
- Publish events to Aeron (currently logged), an end-to-end test, broader unit test coverage, and a GitHub Actions workflow.

## Assumptions
- The project currently assumes limit orders only (FIX `40=2`, `OrdType=Limit`).
- Other order types (for example market, IOC, FOK) are not supported yet.
- Built for only one instrument, need to expand for multiple

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

