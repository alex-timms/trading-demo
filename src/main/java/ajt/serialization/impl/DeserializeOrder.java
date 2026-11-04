package ajt.serialization.impl;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.util.Optional;

public class DeserializeOrder implements Deserializer<Order> {
    private static final int INSTRUMENT_ID = 1;

    @Override
    public Order deserialize(ByteBuffer b) {
        return Optional.ofNullable(b)
                .map(ByteBuffer::asReadOnlyBuffer)
                .map(this::parseFields)
                .map(this::validateOrder)
                .map(this::toOrder)
                .orElseThrow(() -> new IllegalArgumentException("Order payload cannot be null"));
    }

    private ParsedOrderFields parseFields(ByteBuffer buffer) {
        ParsedOrderFields fields = new ParsedOrderFields();

        while (buffer.hasRemaining()) {
            int tag = readTag(buffer);
            if (tag == -1) {
                break;
            }

            switch (tag) {
                case 11 -> {
                    fields.orderId = readLongValue(buffer, "11");
                    fields.hasOrderId = true;
                }
                case 38 -> {
                    fields.qty = readLongValue(buffer, "38");
                    fields.hasQty = true;
                }
                case 44 -> {
                    fields.price = readScaledPrice(buffer);
                    fields.hasPrice = true;
                }
                case 54 -> {
                    fields.side = readSideValue(buffer);
                    fields.hasSide = true;
                }
                default -> skipValue(buffer);
            }
        }

        return fields;
    }

    private ParsedOrderFields validateOrder(ParsedOrderFields fields) {
        if (!fields.hasOrderId) {
            throw new IllegalArgumentException("Missing required tag: 11");
        }
        if (!fields.hasQty) {
            throw new IllegalArgumentException("Missing required tag: 38");
        }
        if (!fields.hasPrice) {
            throw new IllegalArgumentException("Missing required tag: 44");
        }
        if (!fields.hasSide) {
            throw new IllegalArgumentException("Missing required side tag: 54");
        }
        return fields;
    }

    private Order toOrder(ParsedOrderFields fields) {
        return new Order(fields.orderId, INSTRUMENT_ID, fields.side, fields.price, fields.qty);
    }

    private static final class ParsedOrderFields {
        private long orderId;
        private long qty;
        private long price;
        private Side side;
        private boolean hasOrderId;
        private boolean hasQty;
        private boolean hasPrice;
        private boolean hasSide;
    }

    private int readTag(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            int first = buffer.get() & 0xFF;
            if (first == '|') {
                continue;
            }
            if (!isDigit(first)) {
                throw new IllegalArgumentException("Invalid tag start: " + (char) first);
            }

            int tag = first - '0';
            while (buffer.hasRemaining()) {
                int next = buffer.get() & 0xFF;
                if (next == '=') {
                    return tag;
                }
                if (!isDigit(next)) {
                    throw new IllegalArgumentException("Invalid tag character: " + (char) next);
                }
                tag = (tag * 10) + (next - '0');
            }

            throw new IllegalArgumentException("Malformed tag: missing '=' delimiter");
        }

        return -1;
    }

    private long readLongValue(ByteBuffer buffer, String tag) {
        long value = 0L;
        boolean hasDigit = false;

        while (buffer.hasRemaining()) {
            int c = buffer.get() & 0xFF;
            if (c == '|') {
                break;
            }
            if (!isDigit(c)) {
                throw new IllegalArgumentException("Invalid numeric value for tag " + tag);
            }
            hasDigit = true;
            value = (value * 10L) + (c - '0');
        }

        if (!hasDigit) {
            throw new IllegalArgumentException("Missing required tag: " + tag);
        }
        return value;
    }

    private long readScaledPrice(ByteBuffer buffer) {
        long whole = 0L;
        long fractional = 0L;
        int fractionalDigits = 0;
        boolean seenDot = false;
        boolean hasDigit = false;

        while (buffer.hasRemaining()) {
            int c = buffer.get() & 0xFF;
            if (c == '|') {
                break;
            }
            if (c == '.') {
                if (seenDot) {
                    throw new IllegalArgumentException("Invalid numeric value for tag 44");
                }
                seenDot = true;
                continue;
            }
            if (!isDigit(c)) {
                throw new IllegalArgumentException("Invalid numeric value for tag 44");
            }

            hasDigit = true;
            int digit = c - '0';
            if (!seenDot) {
                whole = (whole * 10L) + digit;
            } else {
                if (fractionalDigits >= 4) {
                    throw new IllegalArgumentException("Tag 44 supports up to 4 decimal places");
                }
                fractional = (fractional * 10L) + digit;
                fractionalDigits++;
            }
        }

        if (!hasDigit) {
            throw new IllegalArgumentException("Missing required tag: 44");
        }

        while (fractionalDigits < 4) {
            fractional *= 10L;
            fractionalDigits++;
        }

        return (whole * 10_000L) + fractional;
    }

    private Side readSideValue(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            throw new IllegalArgumentException("Missing required side tag: 54");
        }

        int sideRaw = buffer.get() & 0xFF;
        Side side = switch (sideRaw) {
            case '0' -> Side.BUY;
            case '1' -> Side.SELL;
            default -> throw new IllegalArgumentException("Unsupported side value: " + (char) sideRaw);
        };

        if (buffer.hasRemaining()) {
            int next = buffer.get() & 0xFF;
            if (next != '|') {
                throw new IllegalArgumentException("Invalid side value for tag 54");
            }
        }

        return side;
    }

    private void skipValue(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            int c = buffer.get() & 0xFF;
            if (c == '|') {
                return;
            }
        }
    }

    private boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }
}
