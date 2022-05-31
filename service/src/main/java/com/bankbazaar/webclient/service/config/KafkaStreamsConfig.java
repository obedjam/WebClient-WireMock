package com.bankbazaar.webclient.service.config;
import com.bankbazaar.webclient.core.model.MovieData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamRetryTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import java.io.Serializable;
import java.util.Properties;

@Configuration
public class KafkaStreamsConfig implements Serializable {

    private static final long serialVersionUID = -5557932117156291482L;
    @Value("${spring.datasource.maxRetries}")
    private Integer maxRetries;

    /**
     *Setup retry template bean.
     */
    @Bean
    @StreamRetryTemplate
    RetryTemplate streamRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        RetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetries);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean
    void deserializerConfig()
    {
        Properties props = new Properties();
        props.put("value.deserializer", JsonDeserializer.class.getName());
        props.put(JsonDeserializer.CONFIG_VALUE_CLASS, MovieData.class.getName());
    }
}
