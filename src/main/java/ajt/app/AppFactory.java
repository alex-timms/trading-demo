package ajt.app;

import ajt.config.Configuration;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.serialization.Deserializer;
import ajt.validation.Validator;

public final class AppFactory {

    private AppFactory() {
    }

    public static Engine createEngine() {
        Deserializer<Order> deserializer = Configuration.orderDeserializerConfig();
        Validator<Order> validator = Configuration.compositeOrderRiskValidatorConfig();
        Matcher matcher = Configuration.matcherConfig();
        EventSink sink = Configuration.eventSinkConfig();
        int[] instrumentIds = new int[]{1};

        return Configuration.matchingEngineConfig(
                deserializer,
                validator,
                matcher,
                sink,
                instrumentIds
        );
    }
}
