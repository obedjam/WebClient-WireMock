package com.bankbazaar.webclient.service.service;
import com.bankbazaar.webclient.core.model.MovieData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@Slf4j
public class OmdbApiCLient {

        @Value("${omdb.api.uri}")
        private String uri;

        public Optional<MovieData> fetchMovieDetails(String movieName)
        {
            WebClient client = WebClient.create(uri);
            MovieData response = client.get().uri("/?t="+movieName+"&apikey=39e493d3").retrieve().bodyToMono(MovieData.class).block();
            if(response!=null && response.getResponse().equals(true))
            {
                return Optional.of(response);
            }
            return Optional.empty();
        }

}
