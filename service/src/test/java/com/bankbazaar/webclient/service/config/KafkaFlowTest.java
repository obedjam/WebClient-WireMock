package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.service.producer.KafkaProducer;
import com.bankbazaar.webclient.service.service.FileUtil;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=localhost:9092"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public class KafkaFlowTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private FileUtil fileUtil;

    @Test
    public void kafkaFlowTest()
    {
        File file1 = fileUtil.createFile("Iron Man.txt");
        assertFalse(file1.exists());
        kafkaProducer.sendData("iron-man");
        await().atMost(Durations.TEN_SECONDS).until(file1::exists);

        kafkaProducer.sendData("iron-man");
        assertTrue(file1.exists());

        File file2 = fileUtil.createFile("Inception.txt");
        assertFalse(file2.exists());
        kafkaProducer.sendData("Inception");
        await().atMost(Durations.TEN_SECONDS).until(file2::exists);
    }

}
