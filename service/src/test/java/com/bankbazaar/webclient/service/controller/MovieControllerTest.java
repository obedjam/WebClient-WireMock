package com.bankbazaar.webclient.service.controller;

import com.bankbazaar.webclient.core.model.MovieData;
import com.bankbazaar.webclient.core.model.Response;
import com.bankbazaar.webclient.core.model.Status;
import com.bankbazaar.webclient.service.service.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=localhost:9092"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8282)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FileUtil fileUtil;
    @Value("${omdb.api.test.uri}")
    private String uri;
    @Test
    void movieControllerTest() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = getResource("data.json");
        stubFor(WireMock.get(urlEqualTo("/?t=iron-man&apikey=39e493d3")).willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonData)
                )
        );

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(uri+"/?t=iron-man&apikey=39e493d3");
        HttpResponse httpResponse = httpClient.execute(request);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        MovieData movieData = mapper.readValue(httpResponse.getEntity().getContent(),MovieData.class);

        /**
         * Check file doesn't exist
         */
        File file1 = fileUtil.createFile("Inception");
        assertFalse(file1.exists());

        /**
         * Create file for movie Inception
         * Validate api response
         * Check file creation
         */
        Response response1 = consumeApi("Inception");
        assertEquals(Status.SUBMITTED, response1.getStatus());
        await().atMost(Durations.TEN_SECONDS).until(file1::exists);

        /**
         * Send duplicate
         * Assert file still exits
         */
        assertEquals(Status.SUBMITTED, response1.getStatus());
        assertTrue(file1.exists());

        /**
         * Check file doesn't exist
         */
        File file2 = fileUtil.createFile("Iron Man");
        assertFalse(file2.exists());

        /**
         * Create file for movie Inception
         * Validate api response
         * Check file creation
         * Validate file Content
         */
        Response response2 = consumeApi("Iron-Man");
        assertEquals(Status.SUBMITTED, response2.getStatus());
        await().atMost(Durations.TEN_SECONDS).until(file2::exists);
        assertEquals(movieData, writeObject(file2));

        file1.delete();
        file2.delete();

    }
    private String getResource(String resource) throws IOException {
        Gson gson = new Gson();
        ClassLoader classLoader = gson.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        return new String(inputStream.readAllBytes());
    }
    private Response consumeApi(String name) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult response = mockMvc.perform(get("/movie")
                        .param("name",name))
                .andExpect(status().is(202)).andReturn();
        Response data = objectMapper.readValue(response.getResponse().getContentAsString(), Response.class);
        return data;
    }

    /**
     * Write back file to object for validation
     * @param file
     */
    public MovieData writeObject(File file){
        try(final FileInputStream fileInput = new FileInputStream(file)) {
            final ObjectInputStream input = new ObjectInputStream(fileInput);
            return (MovieData) input.readObject();
        }
        catch (Exception exception)
        {
            return null;
        }
    }
}