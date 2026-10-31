package ajt.engine.impl;

import ajt.engine.Engine;

import java.nio.ByteBuffer;

public class MatchingEngine implements Engine {

    @Override
    public void onNewOrder(ByteBuffer cmd) {
        // 1. stateful pre-trade risk checks; reject and return if they fail
        // 2. look up the OrderBook for cmd.instrumentId
        // 3. matcher.match(order, book, sink)
        // 4. events go to the sink, which publishes them to the Aeron "events" stream

    }
}
