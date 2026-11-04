package ajt.serialization.impl;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DeserializeOrder implements Deserializer<Order> {
    private static final int INSTRUMENT_ID = 1;

    @Override
    public Order deserialize(ByteBuffer b) {
        if (b == null) {
            throw new IllegalArgumentException("Order payload cannot be null");
        }

        String payload = readPayload(b);
        Map<String, String> tags = parseTags(payload);

        long orderId = parseLong(tags, "11");
        long qty = parseLong(tags, "38");
        long price = parsePrice(tags, "44");
        Side side = parseSide(tags);

        return new Order(orderId, INSTRUMENT_ID, side, price, qty);
    }

    private String readPayload(ByteBuffer buffer) {
        ByteBuffer copy = buffer.asReadOnlyBuffer();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseTags(String payload) {
        Map<String, String> tags = new HashMap<>();
        for (String token : payload.split("\\|")) {
            if (token.isEmpty()) {
                continue;
            }
            String[] kv = token.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            tags.put(kv[0], kv[1]);
        }
        return tags;
    }

    private long parseLong(Map<String, String> tags, String key) {
        String value = tags.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required tag: " + key);
        }
        return Long.parseLong(value);
    }

    private long parsePrice(Map<String, String> tags, String key) {
        String value = tags.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required tag: " + key);
        }
        return Math.round(Double.parseDouble(value) * 10_000);
    }

    private Side parseSide(Map<String, String> tags) {
        // Prefer FIX side tag 54, with fallback to tag 11 if caller uses that convention.
        String sideRaw = tags.getOrDefault("54", tags.get("11"));
        if (sideRaw == null) {
            throw new IllegalArgumentException("Missing required side tag: 54");
        }
        return switch (sideRaw) {
            case "0" -> Side.BUY;
            case "1" -> Side.SELL;
            default -> throw new IllegalArgumentException("Unsupported side value: " + sideRaw);
        };
    }
}
