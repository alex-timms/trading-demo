package ajt.serialization.impl;

import ajt.order.Order;
import ajt.serialization.Deserializer;

import java.nio.ByteBuffer;

public class DeserializeOrder implements Deserializer<Order> {
    @Override
    public Order deserialize(ByteBuffer b) {
        return null;
    }
}
