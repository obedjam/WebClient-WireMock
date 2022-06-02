package com.bankbazaar.webclient.service.producer;

import com.bankbazaar.webclient.core.model.ExecutionRequest;
import com.bankbazaar.webclient.core.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class RequestProcessor {
    @Value("${kafka.producer.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, ExecutionRequest> kafkaTemplate;

    public void processRequest(String name)
    {
        ExecutionRequest executionRequest = new ExecutionRequest(name, Status.SUBMITTED);
        this.kafkaTemplate.send(topic,executionRequest);
    }
}