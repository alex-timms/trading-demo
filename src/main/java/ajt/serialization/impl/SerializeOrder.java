package ajt.serialization.impl;

import ajt.common.constants.FIX;
import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerializeOrder implements Serializer<Order> {
    private static final String FIX_VERSION = "FIX.4.4";

    @Override
    public ByteBuffer serialize(Order o) {
        if (o == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (o.side == null) {
            throw new IllegalArgumentException("Order side cannot be null");
        }

        StringBuilder sb = new StringBuilder(96);
        appendTag(sb, 8, FIX_VERSION);
        appendTag(sb, 35, "D");
        appendTag(sb, FIX.TAGS.ORDER_ID_TAG, o.orderId);
        appendTag(sb, FIX.TAGS.INSTRUMENT_ID_TAG, o.instrumentId);
        appendTag(sb, FIX.TAGS.SIDE_TAG, sideValue(o.side));
        appendTag(sb, FIX.TAGS.QUANTITY_TAG, o.leavesQty);
        appendTag(sb, 40, "2");
        appendTag(sb, FIX.TAGS.PRICE_TAG, formatPrice(o.price));

        return ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendTag(StringBuilder sb, int tag, long value) {
        appendTag(sb, tag, String.valueOf(value));
    }

    private void appendTag(StringBuilder sb, int tag, String value) {
        sb.append(tag)
                .append((char) FIX.SYMBOLS.EQUAL)
                .append(value)
                .append(FIX.SYMBOLS.SEPARATOR);
    }

    private String sideValue(Side side) {
        return side == Side.BUY ? "0" : "1";
    }

    private String formatPrice(long scaledPrice) {
        long whole = scaledPrice / 10_000L;
        long fractional = Math.abs(scaledPrice % 10_000L);
        return whole + "." + String.format("%04d", fractional);
    }
}
