package com.bankbazaar.webclient.service.controller;
import com.bankbazaar.webclient.core.model.Response;
import com.bankbazaar.webclient.core.model.Status;
import com.bankbazaar.webclient.service.producer.RequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping(value = "/")
@Slf4j
public class MovieController {

    @Autowired
    private RequestProcessor requestProcessor;

    @RequestMapping(value = "movie", method = RequestMethod.GET)
    public ResponseEntity<Response> postData(@RequestParam String name)
    {

        requestProcessor.processRequest(name);
        Response response = new Response(name, Status.SUBMITTED,new Date());
        return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
    }


}
