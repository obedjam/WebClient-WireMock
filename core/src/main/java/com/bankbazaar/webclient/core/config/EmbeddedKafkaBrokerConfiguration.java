package com.bankbazaar.webclient.core.config;
import java.util.*;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

@Configuration
//@Profile({"dev", "test"})
public class EmbeddedKafkaBrokerConfiguration {

  private static final String TMP_EMBEDDED_KAFKA_LOGS =
          String.format("/tmp/embedded-kafka-logs-%1$s/", UUID.randomUUID());
  private static final String PORT = "port";
  private static final String LOG_DIRS = "log.dirs";
  private static final String LISTENERS = "listeners";
  private static final Integer KAFKA_PORT = 9092;
  private static final String LISTENERS_VALUE = "PLAINTEXT://localhost:" + KAFKA_PORT;
  private static final Integer ZOOKEEPER_PORT = 2181;

  private EmbeddedKafkaBroker embeddedKafkaBroker;

  /**
   * bean for the embeddedKafkaBroker.
   *
   * @return local embeddedKafkaBroker
   */
  @Bean
  public EmbeddedKafkaBroker embeddedKafkaBroker() {
    String[] topics = {"File_Processor", "Create_CSV", "Notification"};
    Map<String, String> brokerProperties = new HashMap<>();
    brokerProperties.put(LISTENERS, LISTENERS_VALUE);
    brokerProperties.put(PORT, KAFKA_PORT.toString());
    brokerProperties.put(LOG_DIRS, TMP_EMBEDDED_KAFKA_LOGS);
    EmbeddedKafkaBroker broker = new EmbeddedKafkaBroker(1, true, topics)
            .kafkaPorts(KAFKA_PORT).zkPort(ZOOKEEPER_PORT);
    broker.brokerProperties(brokerProperties);
    this.embeddedKafkaBroker = broker;
    return broker;
  }

  /**
   * close the embeddedKafkaBroker on destroy.
   */
  @PreDestroy
  public void preDestroy() {
    if (embeddedKafkaBroker != null) {
      embeddedKafkaBroker.destroy();
    }
  }
}
