package com.bankbazaar.webclient.service.producer;

import com.bankbazaar.webclient.core.model.Response;
import com.bankbazaar.webclient.core.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class KafkaProducer {
    @Value("${kafka.producer.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Response> kafkaTemplate;

    public Response sendData(String name)
    {
        Response response = new Response(name, Status.SUBMITTED,new Date());
        this.kafkaTemplate.send(topic,response);
        return response;
    }
}
