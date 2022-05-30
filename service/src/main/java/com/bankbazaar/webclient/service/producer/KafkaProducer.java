package com.bankbazaar.webclient.service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private  static  final  String TOPIC = "Create_File";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendData(String name)
    {
        this.kafkaTemplate.send(TOPIC,name);
    }
}
