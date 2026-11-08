# trading-demo
Matching engine takes in a ByteBuffer in FIX format, deserializes, validates and 
matches the order with appropriate bids/asks based on price-time priority, and rests
any unfulfilled orders.

## Project Structure
```text
trading-demo/
    aeron/          agents and infrastructure for Aeron messaging
    app/            creates the matching engine using the config
    common/         enums, exceptions, custom extensions, groupins
    config/         custom configuration
    engine/         matching engine
    event/          log trade events
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
2. Match orders based on price-time priority.
2. Publish events to Aeron, currently just logged

## Future developments planned
1. Pass multiple orders to main method args
2. Publish events to Aeron, currently just logged
3. Clean up the code and remove unused classes (Order)
4. Integration test between all components
5. Improved unit test coverage

## Assumptions
- The project currently assumes limit orders only (FIX `40=2`, `OrdType=Limit`).
- Other order types (for example market, IOC, FOK) are not supported yet.
