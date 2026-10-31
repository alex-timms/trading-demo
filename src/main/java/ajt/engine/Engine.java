package ajt.engine;

import java.nio.ByteBuffer;

public interface Engine {

    void onNewOrder(ByteBuffer cmd);
}
