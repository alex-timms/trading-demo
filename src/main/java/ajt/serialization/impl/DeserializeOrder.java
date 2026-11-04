package ajt.serialization.impl;

import ajt.common.constants.FIX;
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
                .map(this::parseAndBuildOrder)
                .orElseThrow(() -> new IllegalArgumentException("Order payload cannot be null"));
    }

    private Order parseAndBuildOrder(ByteBuffer buffer) {
        Long orderId = null;
        Long qty = null;
        Long price = null;
        Side side = null;

        while (buffer.hasRemaining()) {
            int tag = readTag(buffer);
            if (tag == -1) {
                break;
            }

            switch (tag) {
                case FIX.TAGS.ORDER_ID_TAG -> orderId = readLongValue(buffer, FIX.TAGS.ORDER_ID_TAG);
                case FIX.TAGS.QUANTITY_TAG -> qty = readLongValue(buffer, FIX.TAGS.QUANTITY_TAG);
                case FIX.TAGS.PRICE_TAG -> price = readScaledPrice(buffer);
                case FIX.TAGS.SIDE_TAG -> side = readSideValue(buffer);
                default -> skipValue(buffer);
            }
        }

        validateOrder(orderId, qty, price, side);
        return new Order(orderId, INSTRUMENT_ID, side, price, qty);
    }

    private int readTag(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            int first = buffer.get() & 0xFF;
            if (first == FIX.SYMBOLS.SEPARATOR) {
                continue;
            }
            if (!isDigit(first)) {
                throw new IllegalArgumentException("Invalid tag start: " + (char) first);
            }

            int tag = first - '0';
            while (buffer.hasRemaining()) {
                int next = buffer.get() & 0xFF;
                if (next == FIX.SYMBOLS.EQUAL) {
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

    private long readLongValue(ByteBuffer buffer, int tag) {
        long value = 0L;
        boolean hasDigit = false;

        while (buffer.hasRemaining()) {
            int c = buffer.get() & 0xFF;
            if (c == FIX.SYMBOLS.SEPARATOR) {
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
            if (c == FIX.SYMBOLS.SEPARATOR) {
                break;
            }
            if (c == '.') {
                if (seenDot) {
                    throw new IllegalArgumentException("Invalid numeric value for tag " + FIX.TAGS.PRICE_TAG);
                }
                seenDot = true;
                continue;
            }
            if (!isDigit(c)) {
                throw new IllegalArgumentException("Invalid numeric value for tag " + FIX.TAGS.PRICE_TAG);
            }

            hasDigit = true;
            int digit = c - '0';
            if (!seenDot) {
                whole = (whole * 10L) + digit;
            } else {
                if (fractionalDigits >= 4) {
                    throw new IllegalArgumentException("Tag " + FIX.TAGS.PRICE_TAG + " supports up to 4 decimal places");
                }
                fractional = (fractional * 10L) + digit;
                fractionalDigits++;
            }
        }

        if (!hasDigit) {
            throw new IllegalArgumentException("Missing required tag: " + FIX.TAGS.PRICE_TAG);
        }

        while (fractionalDigits < 4) {
            fractional *= 10L;
            fractionalDigits++;
        }

        return (whole * 10_000L) + fractional;
    }

    private Side readSideValue(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            throw new IllegalArgumentException("Missing required side tag: " + FIX.TAGS.SIDE_TAG);
        }

        int sideRaw = buffer.get() & 0xFF;
        Side side = switch (sideRaw) {
            case '0' -> Side.BUY;
            case '1' -> Side.SELL;
            default -> throw new IllegalArgumentException("Unsupported side value: " + (char) sideRaw);
        };

        if (buffer.hasRemaining()) {
            int next = buffer.get() & 0xFF;
            if (next != FIX.SYMBOLS.SEPARATOR) {
                throw new IllegalArgumentException("Invalid side value for tag " + FIX.TAGS.SIDE_TAG);
            }
        }

        return side;
    }

    private void validateOrder(Long orderId, Long qty, Long price, Side side) {
        if (orderId == null) {
            throw new IllegalArgumentException("Missing required tag: " + FIX.TAGS.ORDER_ID_TAG);
        }
        if (qty == null) {
            throw new IllegalArgumentException("Missing required tag: " + FIX.TAGS.QUANTITY_TAG);
        }
        if (price == null) {
            throw new IllegalArgumentException("Missing required tag: " + FIX.TAGS.PRICE_TAG);
        }
        if (side == null) {
            throw new IllegalArgumentException("Missing required side tag: " + FIX.TAGS.SIDE_TAG);
        }
    }

    private void skipValue(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            int c = buffer.get() & 0xFF;
            if (c == FIX.SYMBOLS.SEPARATOR) {
                return;
            }
        }
    }

    private boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }
}
