package com.bankbazaar.webclient.service.config;

import com.bankbazaar.webclient.core.model.ExecutionRequest;
import com.bankbazaar.webclient.core.model.MovieData;
import com.bankbazaar.webclient.core.model.Status;
import com.bankbazaar.webclient.service.service.ExecutionRequestService;
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
public class MovieProcessorFlow implements Serializable {

    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private ExecutionRequestService requestService;
    @Autowired
    private OmdbApiCLient omdbApiCLient;
    /**
     * Set status to IN_PROGRESS.
     * Consumes data from Create_CSV topic.
     * Validates if file already exits.
     * Output data to File_Processor topic.
     */

    @Bean
    public Consumer<KStream<String, ExecutionRequest>> fileProcessor(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                        retryTemplate.execute(
                                retryContext ->
                                {
                                        ExecutionRequest request = requestService.updateStatus(value,Status.PROCESSING);
                                        if (fileUtil.fileExists(request.getName())) {
                                            request.setStatus(Status.FAILURE);
                                        }
                                        return new KeyValue<>(key, request);
                                },
                                context ->
                                {
                                    log.error("retries exhausted",context.getLastThrowable());
                                    return new KeyValue<>(key,requestService.updateStatus(value,Status.ERROR));
                                }
                        )
                ).filter((key, value) -> value != null).split()
                .branch(
                        (key, value) -> !requestService.isEndState(value.getStatus()),
                        Branched.withConsumer(stream -> stream.to("File_Processor", Produced.with(Serdes.String(), ExecutionRequestSerdes.ExecutionRequestSerde())))
                )
                .branch(
                        (key, value) -> requestService.isEndState(value.getStatus()),
                        Branched.withConsumer(stream -> stream.to("Notification",Produced.with(Serdes.String(), ExecutionRequestSerdes.ExecutionRequestSerde())))
                );
    }

    /**
     * Consumes data from File_Processor topic.
     * Create file and populate file using movie data.
     * Retry a maximum of 1 time if exception occurs;
     */
    @Bean
    public Function<KStream<String, ExecutionRequest>, KStream<String,ExecutionRequest>> consumer(@Qualifier("streamRetryTemplate") RetryTemplate retryTemplate)
    {
        return kStream -> kStream.map((key, value) ->
                retryTemplate.execute(
                        retryContext ->
                        {
                            Optional<MovieData> response = omdbApiCLient.fetchMovieDetails(value.getName());
                            ExecutionRequest request = requestService.updateStatus(value,Status.PROCESSING);
                            if(response.isPresent()) {
                                if(fileUtil.writeFile(response.get())) {
                                    request.setStatus(Status.SUCCESS);
                                    return new KeyValue<>(key, request);
                                }
                                else
                                {
                                    request.setStatus(Status.ERROR);
                                    return new KeyValue<>(key, request);
                                }
                            }
                            request.setStatus(Status.FAILURE);
                            return new KeyValue<>(key, request);
                        }, context -> {
                            log.error("retries exhausted",context.getLastThrowable());
                            return new KeyValue<>(key, requestService.updateStatus(value, Status.ERROR));
                        }
                )
        ).filter((key, value) -> value != null);

    }

    @Bean
    public Consumer<KStream<String,ExecutionRequest>> notification()
    {
        return kStream -> kStream.foreach((key, value) ->
        {
            log.info(value.getStatus().toString());
        });
    }
}