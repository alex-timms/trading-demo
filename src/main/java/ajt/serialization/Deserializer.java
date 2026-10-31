package ajt.serialization;

import java.nio.ByteBuffer;

public interface Deserializer<T> {

    T deserialize(ByteBuffer b);
}
