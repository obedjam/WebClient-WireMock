package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.MovieData;
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
    public Consumer<KStream<String, String>> fileProcessor(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                        retryTemplate.execute(
                                retryContext ->
                                {
                                    Optional<MovieData> response = omdbApiCLient.fetchMovieDetails(value);
                                    if(response.isPresent()) {
                                        if (fileUtil.createFile(response.get().getTitle()).exists()) {
                                            response.get().setResponse(false);
                                        }
                                        return new KeyValue<>(key, response.get());
                                    }
                                    return new KeyValue<>(key, null);
                                },
                                context ->
                                {
                                    log.error("retries exhausted",context.getLastThrowable());
                                    return new KeyValue<>(key,null);
                                }
                        )
                ).filter((key, value) -> value != null).split()
                .branch(
                        (key, value) -> value.getResponse().equals(true),
                        Branched.withConsumer(stream -> stream.to("File_Processor", Produced.with(Serdes.String(), MovieDataSerdes.MovieDataSerde())))
                )
                .branch(
                        (key, value) -> value.getResponse().equals(false),
                        Branched.withConsumer(stream -> stream.to("Notification",Produced.with(Serdes.String(), MovieDataSerdes.MovieDataSerde())))
                );
    }

    /**
     * Consumes data from File_Processor topic.
     * Create file and populate file using movie data.
     * Retry a maximum of 1 time if exception occurs;
     */
    @Bean
    public Function<KStream<String, MovieData>, KStream<String,MovieData>> consumer(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                retryTemplate.execute(
                        retryContext ->
                        {
                            value.setResponse(fileUtil.writeFile(value));
                            return new KeyValue<>(key,value);
                        }, context -> {
                            log.error("retries exhausted",context.getLastThrowable());
                            value.setResponse(false);
                            return new KeyValue<>(key,value);
                        }
                )
        ).filter((key, value) -> value != null);

    }

    @Bean
    public Consumer<KStream<String,MovieData>> notification()
    {
        return kStream -> kStream.foreach((key, value) ->
        {
            if(value.getResponse())
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
