package com.bankbazaar.webclient.service.controller;
import com.bankbazaar.webclient.service.producer.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
@Slf4j
public class MovieController {

    @Autowired
    private KafkaProducer kafkaProducer;

    @RequestMapping(value = "movie", method = RequestMethod.GET)
    public ResponseEntity<String> postData(@RequestParam String name)
    {
        kafkaProducer.sendData(name);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


}
