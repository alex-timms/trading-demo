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
        publisher.publishString("Order accepted: " + order.orderId);
    }

    @Override
    public void fill(Order aggressor, Order resting, long price, long qty) {
        System.out.println("Order fill");
    }

    public void rest(Order aggressor) {
        System.out.println("Order rested");
    }
}
