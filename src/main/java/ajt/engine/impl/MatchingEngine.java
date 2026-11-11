package ajt.engine.impl;

import ajt.common.group.OrderGroup;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.order.books.OrderBookImpl;
import ajt.serialization.Deserializer;
import ajt.validation.Validator;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
// ToDo: Lombok
public class MatchingEngine implements Engine {

    private final Map<Integer, ajt.order.OrderBook> books = new HashMap<>();

    Deserializer<Order> deserializer;
    Validator<Order> validator;
    Matcher matcher;
    EventSink eventSink;

    public MatchingEngine(Deserializer<Order> deserializer, Validator<Order> validator, Matcher matcher, EventSink eventSink, int[] ids) {
        this.deserializer = deserializer;
        this.validator = validator;
        this.matcher = matcher;
        this.eventSink = eventSink;
        for (int i : ids) {
            books.put(i, new OrderBookImpl());
        }
    }

    @Override
    public void onNewOrder(ByteBuffer cmd) {
        // 1. stateful pre-trade risk checks; reject and return if they fail
        // 2. look up the OrderBook for cmd.instrumentId
        // 3. matcher.match(order, book, sink)
        // 4. events go to the sink, which publishes them to the Aeron "events" stream

        System.out.println("MatchingEngine.onNewOrder Received");
        Optional.of(cmd)
                .map(deserializer::deserialize)
                .filter(validator::validate)
                .map(order -> {
                    ajt.order.OrderBook book = resolvedOrderBook(order);
                    return new OrderGroup(order, book);
                })
                .ifPresent(og -> matcher.match(og.order(), og.book()));
    }

    private ajt.order.OrderBook resolvedOrderBook(Order order) {
        return books.get(order.instrumentId);
    }
}
