package com.rajesh.wiremock.service;

import com.rajesh.wiremock.constants.MoviesAppConstants;
import com.rajesh.wiremock.dto.Movie;
import com.rajesh.wiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient webClient){
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies(){
        return webClient.get()
                .uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
    }

    public Movie retrieveMovieById(Integer movieId){

        //http://localhost:8081/movieservice/v1/movie/1
        try{
            return webClient.get()
                    .uri(MoviesAppConstants.GET_MOVIE_BY_ID, movieId)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieById. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieById. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> retrieveMoviesByName(String name){

        //http://localhost:8081/movieservice/v1/movieName?movie_name=Avengers
        try{

            String uri = UriComponentsBuilder.fromUriString(MoviesAppConstants.GET_MOVIES_BY_NAME)
                    .queryParam("movie_name", name)
                    .buildAndExpand()
                    .toUriString();

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieByName. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieByName. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> retrieveMoviesByYear(Integer movieYear){

        //http://localhost:8081/movieservice/v1/movieYear?year=2012
        try{

            String uri = UriComponentsBuilder.fromUriString(MoviesAppConstants.GET_MOVIES_BY_YEAR)
                    .queryParam("year", movieYear)
                    .buildAndExpand()
                    .toUriString();

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieByYear. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieByYear. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie addMovie(Movie movie){
        try {
            return webClient.post()
                    .uri(MoviesAppConstants.ADD_MOVIE_V1)
                    .syncBody(movie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in addMovie. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in addMovie. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie updateMovie(Integer movieId, Movie movie){
        try {
            return webClient.put()
                    .uri(MoviesAppConstants.UPDATE_MOVIE_V1, movieId)
                    .syncBody(movie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in updateMovie. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in updateMovie. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

    public String deleteMovie(Integer movieId){
        try {
            return webClient.delete()
                    .uri(MoviesAppConstants.DELETE_MOVIE_BY_ID_V1, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in deleteMovie. Status code is {} and message is {}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in deleteMovie. Message is {}",
                    ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }

}
