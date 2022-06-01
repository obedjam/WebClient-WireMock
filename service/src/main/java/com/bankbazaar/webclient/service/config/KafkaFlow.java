package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.MovieData;
import com.bankbazaar.webclient.core.model.Response;
import com.bankbazaar.webclient.core.model.Status;
import com.bankbazaar.webclient.service.service.FileUtil;
import com.bankbazaar.webclient.service.service.OmdbApiCLient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class KafkaFlow implements Serializable {

    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private OmdbApiCLient omdbApiCLient;
    /**
     * Set status to IN_PROGRESS.
     * Consumes data from Create_CSV topic.
     * Validates if file already exits.
     * Output data to File_Processor topic.
     */

    @Bean
    public Consumer<KStream<String, Response>> fileProcessor(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                        retryTemplate.execute(
                                retryContext ->
                                {
                                        value.setStatus(Status.PROCESSING);
                                        if (fileUtil.createFile(value.getName()).exists()) {
                                            value.setStatus(Status.FAILURE);
                                        }
                                        return new KeyValue<>(key, value);
                                },
                                context ->
                                {
                                    log.error("retries exhausted",context.getLastThrowable());
                                    value.setStatus(Status.ERROR);
                                    return new KeyValue<>(key,value);
                                }
                        )
                ).filter((key, value) -> value != null).split()
                .branch(
                        (key, value) -> value.getStatus().ordinal()<=1,
                        Branched.withConsumer(stream -> stream.to("File_Processor", Produced.with(Serdes.String(), ResponseSerdes.ResponseSerde())))
                )
                .branch(
                        (key, value) -> value.getStatus().ordinal()>1,
                        Branched.withConsumer(stream -> stream.to("Notification",Produced.with(Serdes.String(), ResponseSerdes.ResponseSerde())))
                );
    }

    /**
     * Consumes data from File_Processor topic.
     * Create file and populate file using movie data.
     * Retry a maximum of 1 time if exception occurs;
     */
    @Bean
    public Function<KStream<String, Response>, KStream<String,Response>> consumer(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                retryTemplate.execute(
                        retryContext ->
                        {
                            Optional<MovieData> response = omdbApiCLient.fetchMovieDetails(value.getName());
                            if(response.isPresent()) {
                                if(fileUtil.writeFile(response.get())) {
                                    value.setStatus(Status.SUCCESS);
                                    return new KeyValue<>(key, value);
                                }
                            }
                            value.setStatus(Status.FAILURE);
                            return new KeyValue<>(key, value);
                        }, context -> {
                            log.error("retries exhausted",context.getLastThrowable());
                            value.setStatus(Status.ERROR);
                            return new KeyValue<>(key, value);
                        }
                )
        ).filter((key, value) -> value != null);

    }

    @Bean
    public Consumer<KStream<String,Response>> notification()
    {
        return kStream -> kStream.foreach((key, value) ->
        {
            log.info(value.getStatus().toString());
        });
    }
}
