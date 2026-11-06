package ajt.app;

import ajt.config.EngineConfiguration;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.serialization.Deserializer;
import ajt.validation.Validator;

public final class EngineFactory {

    private EngineFactory() {
    }

    public static Engine createMatchingEngine() {
        Deserializer<Order> deserializer = EngineConfiguration.orderDeserializer();
        Validator<Order> validator = EngineConfiguration.compositeOrderRiskValidator();
        EventSink sink = EngineConfiguration.eventSink();
        Matcher matcher = EngineConfiguration.matcher(sink);
        int[] instrumentIds = new int[]{1}; // ToDo: Add instrumentIds that are allowed

        return EngineConfiguration.matchingEngine(
                deserializer,
                validator,
                matcher,
                sink,
                instrumentIds
        );
    }
}
