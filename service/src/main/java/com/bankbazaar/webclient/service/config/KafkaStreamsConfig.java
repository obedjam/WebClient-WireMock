package com.bankbazaar.webclient.service.config;
import com.bankbazaar.webclient.service.service.OmdbApiCLient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamRetryTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Configuration
public class KafkaStreamsConfig implements Serializable {

    private static final long serialVersionUID = -5557932117156291482L;
    @Value("${spring.datasource.maxRetries}")
    private Integer maxRetries;

    @Autowired
    private OmdbApiCLient omdbApiCLient;

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
    /**
     * Set status to IN_PROGRESS.
     * Consumes data from Create_CSV topic.
     * Validates if file already exits.
     * Output data to File_Processor topic.
     */

    @Bean
    public Consumer<KStream<String, String>>fileProcessor(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                                retryTemplate.execute(
                                        retryContext ->
                                        {
                                                ClassLoader classLoader = getClass().getClassLoader();
                                                Map response = omdbApiCLient.consumeApi(value);
                                                if(response.get("Response").equals("True"))
                                                {
                                                    response.put("Response", true);
                                                }
                                                else
                                                {
                                                    response.put("Response", false);
                                                }
                                                File file = new File(classLoader.getResource(".").getFile() + response.get("Title").toString()+".txt");
                                                if (file.exists()) {
                                                    response.put("Response", false);
                                                }
                                                return new KeyValue<>(key,response);
                                        },
                                        context ->
                                        {
                                            log.error("retries exhausted",context.getLastThrowable());
                                            Map response = omdbApiCLient.consumeApi(value);
                                            response.put("Response", false);
                                            return new KeyValue<>(key,response);
                                        }
                                )
                ).filter((key, value) -> value != null).split()
                .branch(
                        (key, value) -> value.get("Response").equals(true),
                        Branched.withConsumer(stream -> stream.to("File_Processor",Produced.with(Serdes.String(), MapSerdes.MapSerde())))
                )
                .branch(
                        (key, value) -> value.get("Response").equals(false),
                        Branched.withConsumer(stream -> stream.to("Notification",Produced.with(Serdes.String(), MapSerdes.MapSerde())))
                );
    }

    /**
     * Consumes data from File_Processor topic.
     * Create.csv file and populate file using data.
     * Retry a maximum of 1 time if exception occurs;
     */
    @Bean
    public Function<KStream<String,Map>, KStream<String,Map>> consumer(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                        retryTemplate.execute(
                                retryContext -> {
                                    try{
                                        ClassLoader classLoader = getClass().getClassLoader();
                                        File file = new File(classLoader.getResource(".").getFile() + value.get("Title").toString()+".txt");
                                        FileWriter fileWriter = new FileWriter(file,false);
                                        for(Object entry: value.entrySet()) {
                                            fileWriter.write(entry.toString()+"\n");
                                        }
                                        fileWriter.close();
                                        return new KeyValue<>(key,value);
                                    }
                                    catch (Exception exception)
                                    {
                                        log.error("retrying",exception);
                                        throw new RuntimeException(exception);
                                    }
                                }, context -> {
                                    log.error("retries exhausted",context.getLastThrowable());
                                    value.put("Response", false);
                                    return new KeyValue<>(key,value);
                                }
                                )
        ).filter((key, value) -> value != null);

    }

    @Bean
    public Consumer<KStream<String,Map>> notification()
    {
        return kStream -> kStream.foreach((key, value) ->
        {
            if(value.get("Response").equals(true))
            {
                log.info("SUCCESS");
            }
            else
            {
                log.info("FAILURE");
            }
        });
    }
}
