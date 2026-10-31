package ajt.config;

import ajt.engine.impl.MatchingEngine;
import ajt.events.EventSink;
import ajt.events.impl.LogEvents;
import ajt.matcher.Matcher;
import ajt.matcher.impl.PriceTimeMatcher;
import ajt.order.Order;
import ajt.serialization.Deserializer;
import ajt.serialization.impl.DeserializeOrder;
import ajt.validation.Validator;
import ajt.validation.impl.RiskValidator;

public class Configuration {

    public static MatchingEngine matchingEngineConfig(
            Deserializer<Order> deserializer,
            Validator<Order> validator,
            Matcher matcher,
            EventSink eventSink,
            int[] ids) {
        return new MatchingEngine(deserializer, validator, matcher, eventSink, ids);
    }

    public static Deserializer<Order> orderDeserializerConfig() {
        return new DeserializeOrder();
    }

    public static Validator<Order> orderValidatorConfig() {
        return new RiskValidator();
    }

    public static Matcher matcherConfig() {
        return new PriceTimeMatcher();
    }

    // ToDo: Aeron events
    public static EventSink eventSinkConfig() {
        return new LogEvents();
    }

}
