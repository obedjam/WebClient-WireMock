package com.bankbazaar.webclient.service.service;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class WebClientService {

        private WebClient client = WebClient.create("http://www.omdbapi.com");

        public Map consumeApi(String movieName)
        {
            return client.get().uri("/?t="+movieName+"&apikey=39e493d3").retrieve().bodyToMono(Map.class).block();
        }

}
