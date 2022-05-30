package com.bankbazaar.webclient.core.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Map;

public final class MapSerdes {
    public static Serde<Map> DataSerde() {
        JsonSerializer<Map> serializer = new JsonSerializer<>();
        JsonDeserializer<Map> deserializer = new JsonDeserializer<>(Map.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}