package ajt.events.impl;

import ajt.aeron.infrastructure.AeronEndpoints;
import ajt.aeron.infrastructure.AeronPayloadPublisher;
import ajt.common.constants.AERON;
import ajt.events.EventSink;
import ajt.order.Order;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.IdleStrategy;

public class PublishEvents implements EventSink {

    AeronPayloadPublisher publisher;

    public PublishEvents(AeronPayloadPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void accept(Order order) {
        publisher.publishString(String.format("Order accepted: %d", order.orderId));
    }

    @Override
    public void fill(Order aggressor, Order resting, long price, long qty) {
        publisher.publishString(String.format(
                "Order fill: %d (%s) with resting order: %d (%s) at price: %d for quantity: %d%n",
                aggressor.orderId,
                aggressor.side,
                resting.orderId,
                resting.side,
                price,
                qty
        ));
    }

    @Override
    public void rest(Order order) {
        publisher.publishString(String.format("Rest Order : " + order));
    }
}
