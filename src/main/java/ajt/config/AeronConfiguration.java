package ajt.config;


import ajt.common.constants.AERON;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

public class AeronConfiguration {
    public record AeronRuntime(MediaDriver driver, Aeron client) {
    }

    public static AeronRuntime startRuntime() {
        MediaDriver.Context driverContext = new MediaDriver.Context()
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true);
        MediaDriver driver = MediaDriver.launch(driverContext);

        Aeron.Context clientContext = new Aeron.Context()
                .aeronDirectoryName(driver.aeronDirectoryName());
        Aeron client = Aeron.connect(clientContext);

        return new AeronRuntime(driver, client);
    }

    public static IdleStrategy idleStrategy() {
        return new SleepingIdleStrategy();
    }

    public static Publication addPub(Aeron aeron, int streamId) {
        return aeron.addPublication(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }

    public static Subscription addSub(Aeron aeron, int streamId) {
        return aeron.addSubscription(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }
}
