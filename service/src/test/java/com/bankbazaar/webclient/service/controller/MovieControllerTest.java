package com.bankbazaar.webclient.service.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=localhost:9092"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public class MovieControllerTest {
    @Value("${omdb.api.uri}")
    private String uri;
    @Test
    public void webClientTest() throws IOException {
        WireMockServer wireMockServer = new WireMockServer();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        String jsonData = getResource("data.json");

        wireMockServer.start();
        stubFor(get(urlEqualTo(uri+"/?t=iron-man&apikey=39e493d3")).willReturn(aResponse()
                                .withStatus(200)
                                .withBody(String.valueOf(equalTo(jsonData)))
                )
        );

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(uri+"/?t=iron-man&apikey=39e493d3");
        HttpResponse httpResponse = httpClient.execute(request);

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        wireMockServer.stop();
    }
    public String getResource(String resource) throws IOException {
        Gson gson = new Gson();
        ClassLoader classLoader = gson.getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            String result = new String(inputStream.readAllBytes());
            return result;
    }
}
