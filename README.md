# trading-demo
Matching engine takes in a ByteBuffer in FIX format, deserializes, validates and 
matches the order with appropriate bids/asks based on price-time priority, and rests
any unfulfilled orders.

## Project Structure
trading-demo/
    common/         enums, exceptions, custom extensions, groupins
    config/         custom configuration
    engine/         matching engine
    event/          log trade events
    matcher/        price time matcher
    order/          order, order book definition
    serialization/  convert byteBuffer ◄─► order
    validation/     validate trade volume risk

## Project Flow Diagram
Aeron "orders" ──► MatchingEngine ──► risk check ──► Matcher ──► OrderBook
│                             │
└────────── events ◄──────────┘ ──► Aeron "events"

## Future developments planned
1. Full unit test coverage
2. Add deserializers that take a bytebuffer in FIX format and convert to orders
2. Publish events to Aeron, currently just logged
3. Integration test between all components