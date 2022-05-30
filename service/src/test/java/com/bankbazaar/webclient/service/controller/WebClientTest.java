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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class WebClientTest {

    @Test
    public void webClientTest() throws IOException {
        WireMockServer wireMockServer = new WireMockServer();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        String jsonData = getResource("data.json");

        wireMockServer.start();
        configureFor("localhost", 8080);
        stubFor(get(urlEqualTo("/movie?name=iron-man"))
                                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(jsonData)
                ));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:8080/movie?name=iron-man");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo("/movie?name=iron-man")));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals("application/json", httpResponse.getFirstHeader("Content-Type").getValue());

        Map jsonMap = mapper.readValue(httpResponse.getEntity().getContent(), Map.class);
        assertEquals(true, jsonMap.get("Response"));
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
