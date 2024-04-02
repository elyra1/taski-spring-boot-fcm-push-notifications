package fcmpushnotifications.firebase;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.cloud.Timestamp;

import java.io.IOException;

public class TimestampDeserializer extends JsonDeserializer<Timestamp> {

    @Override
    public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        long timestamp = jsonParser.getValueAsLong();
        return Timestamp.ofTimeMicroseconds(timestamp * 1000);
    }
}