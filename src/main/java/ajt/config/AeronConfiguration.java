package ajt.config;


import ajt.common.constants.AERON;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

public class AeronConfiguration {
    // set channel type

    // set up idle strategy
    private final IdleStrategy idle = new SleepingIdleStrategy();

    public static Aeron aeronStart() {
        MediaDriver driver = MediaDriver.launch();
        return Aeron.connect();
    }

    public static IdleStrategy idleStrategy() {
        return new SleepingIdleStrategy();
    }

    public static Publication addPub(Aeron aeron, Integer streamId) {
        return aeron.addPublication(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }

    public static Subscription addSub(Aeron aeron, Integer streamId) {
        return aeron.addSubscription(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }
}
