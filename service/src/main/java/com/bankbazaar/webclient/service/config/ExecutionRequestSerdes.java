package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.ExecutionRequest;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public final class ExecutionRequestSerdes {
    public static Serde<ExecutionRequest> ExecutionRequestSerde() {
        JsonSerializer<ExecutionRequest> serializer = new JsonSerializer<>();
        JsonDeserializer<ExecutionRequest> deserializer = new JsonDeserializer<>(ExecutionRequest.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}