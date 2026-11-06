package ajt.aeron.infrastructure;

import ajt.config.AeronConfiguration;
import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.IdleStrategy;

public final class AeronFactory {

    private AeronFactory() {
    }

    public record AeronRuntime(MediaDriver driver, Aeron client) {
    }

    public static AeronRuntime startRuntime() {
        MediaDriver.Context driverContext = AeronConfiguration.mediaDriver();
        MediaDriver driver = MediaDriver.launch(driverContext);

        Aeron.Context clientContext = AeronConfiguration.aeronContext(driver);
        Aeron client = Aeron.connect(clientContext);

        return new AeronRuntime(driver, client);
    }

}

