package com.bankbazaar.webclient.service.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=localhost:9092"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
public class MovieControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Value("${omdb.api.uri}")
    private String uri;

    @Test
    public void webClientTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        String jsonData = getResource("data.json");

        stubFor(WireMock.get(urlEqualTo(uri+"/?t=iron-man&apikey=39e493d3")).willReturn(aResponse()
                                .withStatus(200)
                                .withBody(String.valueOf(equalTo(jsonData)))
                )
        );

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(uri+"/?t=iron-man&apikey=39e493d3");
        HttpResponse httpResponse = httpClient.execute(request);

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        consumeApi("iron-man");

    }
    private String getResource(String resource) throws IOException {
        Gson gson = new Gson();
        ClassLoader classLoader = gson.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        return new String(inputStream.readAllBytes());
    }

    private void consumeApi(String name) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult response = mockMvc.perform(get("/movie")
                        .param("name","iron-man"))
                        .andExpect(status().is(202)).andReturn();
    }
}
