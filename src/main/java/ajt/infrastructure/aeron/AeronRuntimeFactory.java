package ajt.infrastructure.aeron;

import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

public final class AeronRuntimeFactory {

    private AeronRuntimeFactory() {
    }

    public record AeronRuntime(MediaDriver driver, Aeron client, IdleStrategy idleStrategy) {
    }

    public static AeronRuntime startRuntime() {
        MediaDriver.Context driverContext = mediaDriverConfig();
        MediaDriver driver = MediaDriver.launch(driverContext);

        Aeron.Context clientContext = aeronConfig(driver);
        Aeron client = Aeron.connect(clientContext);

        return new AeronRuntime(driver, client, idleStrategy());
    }

    public static MediaDriver.Context mediaDriverConfig() {
        return new MediaDriver.Context()
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true);
    }

    public static Aeron.Context aeronConfig(MediaDriver driver) {
        return new Aeron.Context()
                .aeronDirectoryName(driver.aeronDirectoryName());
    }

    public static IdleStrategy idleStrategy() {
        return new SleepingIdleStrategy();
    }
}

