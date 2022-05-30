package com.bankbazaar.webclient.service.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Map;

public final class MapSerdes {
    public static Serde<Map> MapSerde() {
        JsonSerializer<Map> serializer = new JsonSerializer<>();
        JsonDeserializer<Map> deserializer = new JsonDeserializer<>(Map.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}