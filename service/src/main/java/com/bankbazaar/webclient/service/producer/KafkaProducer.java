package com.bankbazaar.webclient.service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducer {
    private  static  final  String TOPIC = "Create_File";

    @Autowired
    private KafkaTemplate<String, Map> kafkaTemplate;

    public void sendData(Map data)
    {
        this.kafkaTemplate.send(TOPIC,data);
    }
}
