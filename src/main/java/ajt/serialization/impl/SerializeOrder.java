package ajt.serialization.impl;

import ajt.order.Order;
import ajt.serialization.Serializer;

import java.nio.ByteBuffer;

public class SerializeOrder implements Serializer<Order> {
    @Override
    public ByteBuffer serialize(Order o) {
        return null;
    }
}
