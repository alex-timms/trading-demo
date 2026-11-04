package ajt;

import ajt.common.constants.AERON;
import ajt.config.AeronConfiguration;
import ajt.engine.Engine;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.Agent;

import java.nio.ByteBuffer;

public class EngineConsumer implements Agent, AutoCloseable {


    private final Aeron aeron;
    private Subscription sub;
    private Engine engine;
    private final FragmentHandler handler = (buffer, offset, length, header) -> {
        byte[] payload = new byte[length];
        buffer.getBytes(offset, payload);          // copy from DirectBuffer slice
        ByteBuffer cmd = ByteBuffer.wrap(payload); // hand off to existing engine API
        engine.onNewOrder(cmd);
    };

    public EngineConsumer(Aeron aeron, Engine engine) {
        this.aeron = aeron;
        this.engine = engine;
    }

    @Override
    public void onStart() {
        System.out.println("Starting EngineConsumer");
        // create subscriber to channel 2
        sub = AeronConfiguration.addSub(aeron, AERON.CHANNELS.INCOMING_ID);
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
        sub.poll(handler, 1000);
        return 0;
    }

    @Override
    public String roleName() {
        return "reroute-service";
    }

    @Override
    public void close() {
        onClose();
    }
}
