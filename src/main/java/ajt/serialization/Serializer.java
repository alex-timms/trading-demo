package ajt.serialization;

import java.nio.ByteBuffer;

public interface Serializer<T> {
    ByteBuffer serialize(T o);
}
