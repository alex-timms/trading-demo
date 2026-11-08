package ajt.aeron.agents;

import ajt.common.constants.AERON;
import ajt.engine.Engine;
import ajt.aeron.infrastructure.AeronEndpoints;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.Agent;

import java.nio.ByteBuffer;

public class EngineConsumerAgent implements Agent, AutoCloseable {

    private final Aeron aeron;
    private Subscription sub;
    private final Engine engine;
    private final FragmentHandler handler;

    public EngineConsumerAgent(Aeron aeron, Engine engine) {
        this.aeron = aeron;
        this.engine = engine;
        this.handler = (buffer, offset, length, header) -> {
            System.out.println("Received message");
            byte[] payload = new byte[length];
            buffer.getBytes(offset, payload);
            ByteBuffer cmd = ByteBuffer.wrap(payload);
            this.engine.onNewOrder(cmd);
        };
    }

    @Override
    public void onStart() {
        System.out.println("Starting EngineConsumer");
        sub = AeronEndpoints.addSubscription(aeron, AERON.STREAMS.INCOMING_ORDERS);
    }

    @Override
    public void onClose() {
        System.out.println("Closing EngineConsumer");
        if (sub != null) {
            sub.close();
        }
    }

    @Override
    public int doWork() {
        return sub.poll(handler, 100);
    }

    @Override
    public String roleName() {
        return "engine-consumer";
    }

    @Override
    public void close() {
        onClose();
    }
}
