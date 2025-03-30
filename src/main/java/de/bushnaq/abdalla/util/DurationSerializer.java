package de.bushnaq.abdalla.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (duration == null) {
            gen.writeNull();
            return;
        }

        StringBuilder sb      = new StringBuilder();
        long          days    = duration.toDays();
        long          hours   = duration.toHoursPart();
        long          minutes = duration.toMinutesPart();
        long          seconds = duration.toSecondsPart();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        gen.writeString(sb.toString().trim());
    }
}