package ajt.config;

import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

public class AeronConfiguration {

    public static MediaDriver.Context mediaDriver() {
        return new MediaDriver.Context()
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true);
    }

    public static Aeron.Context aeronContext(MediaDriver driver) {
        return new Aeron.Context()
                .aeronDirectoryName(driver.aeronDirectoryName());
    }

    public static IdleStrategy idleStrategy() {
        return new SleepingIdleStrategy();
    }
}
