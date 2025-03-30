package de.bushnaq.abdalla.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        long     days  = 0, hours = 0, minutes = 0, seconds = 0;
        String[] parts = value.split("\\s+");

        for (String part : parts) {
            if (part.endsWith("d")) {
                days = Long.parseLong(part.substring(0, part.length() - 1));
            } else if (part.endsWith("h")) {
                hours = Long.parseLong(part.substring(0, part.length() - 1));
            } else if (part.endsWith("m")) {
                minutes = Long.parseLong(part.substring(0, part.length() - 1));
            } else if (part.endsWith("s")) {
                seconds = Long.parseLong(part.substring(0, part.length() - 1));
            }
        }

        return Duration.ofDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
    }
}
