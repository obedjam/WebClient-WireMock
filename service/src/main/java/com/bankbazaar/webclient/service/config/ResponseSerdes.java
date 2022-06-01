package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.Response;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public final class ResponseSerdes {
    public static Serde<Response> ResponseSerde() {
        JsonSerializer<Response> serializer = new JsonSerializer<>();
        JsonDeserializer<Response> deserializer = new JsonDeserializer<>(Response.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}