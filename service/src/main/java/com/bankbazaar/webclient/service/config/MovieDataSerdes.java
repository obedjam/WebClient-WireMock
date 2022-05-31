package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.MovieData;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public final class MovieDataSerdes {
    public static Serde<MovieData> MovieDataSerde() {
        JsonSerializer<MovieData> serializer = new JsonSerializer<>();
        JsonDeserializer<MovieData> deserializer = new JsonDeserializer<>(MovieData.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}