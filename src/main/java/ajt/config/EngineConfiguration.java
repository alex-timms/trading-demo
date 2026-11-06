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
import ajt.validation.impl.CompositeValidator;
import ajt.validation.impl.OrderValidator;
import ajt.validation.impl.RiskValidator;

import java.util.List;

public class EngineConfiguration {

    public static MatchingEngine matchingEngine(
            Deserializer<Order> deserializer,
            Validator<Order> validator,
            Matcher matcher,
            EventSink eventSink,
            int[] ids) {
        return new MatchingEngine(deserializer, validator, matcher, eventSink, ids);
    }

    public static Deserializer<Order> orderDeserializer() {
        return new DeserializeOrder();
    }

    public static Validator<Order> orderValidator() {
        return new OrderValidator();
    }

    public static Validator<Order> riskValidator() {
        return new RiskValidator();
    }

    public static Validator<Order> compositeOrderRiskValidator() {
        return new CompositeValidator<>(List.of(orderValidator(), riskValidator()));
    }

    public static Matcher matcher() {
        return new PriceTimeMatcher();
    }

    // ToDo: Aeron events
    public static EventSink eventSink() {
        return new LogEvents();
    }

}
