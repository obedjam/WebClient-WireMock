package com.bankbazaar.webclient.service.controller;
import com.bankbazaar.webclient.service.producer.KafkaProducer;
import com.bankbazaar.webclient.service.service.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/")
@Slf4j
public class WebClientController {

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @RequestMapping(value = "movie", method = RequestMethod.GET)
    public ResponseEntity<Map> postData(@RequestParam String name)
    {
        Map response = webClientService.consumeApi(name);
        if(response.get("Response").toString().equals("True"))
        {
            response.put("Response", true);
            kafkaProducer.sendData(response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


}
