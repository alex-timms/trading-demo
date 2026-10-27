package ajt.engine;

public interface EventSink {
    void accepted(Order order);
    void fill(Order aggressor, Order resting, long price, long qty);
}
