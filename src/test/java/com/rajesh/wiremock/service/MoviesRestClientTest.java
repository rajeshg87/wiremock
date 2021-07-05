package com.rajesh.wiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.rajesh.wiremock.constants.MoviesAppConstants;
import com.rajesh.wiremock.dto.Movie;
import com.rajesh.wiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {

    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s",port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void testRetrieveAllMovies() {

        //Given
        stubFor(get(anyUrl())
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //When
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println(movieList);

        //Then
        assertTrue(movieList.size() > 0);
    }

    @Test
    void testRetrieveAllMovies_MatchStubUrl() {

        //Given
        stubFor(get(urlEqualTo(MoviesAppConstants.GET_ALL_MOVIES_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //When
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println(movieList);

        //Then
        assertTrue(movieList.size() > 0);
    }

    @Test
    void testRetrieveMovieById() {
        //Given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));
        Integer movieId = 1;

        //When
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //Then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void testRetrieveMovieById_ResponseTemplate() {
        //Given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-template.json")));
        Integer movieId = 3;

        //When
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //Then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void testRetrieveMovieById_NotFound() {
        //Given
        Integer movieId = 100;

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void testRetrieveMovieByName() {
        //Given
        String movieName = "Avengers";

        //When
        List<Movie> movies = moviesRestClient.retrieveMoviesByName(movieName);

        //Then
        assertEquals(4, movies.size());
    }

    @Test
    void testRetrieveMovieByName_NotFound() {
        //Given
        String movieName = "ABC";

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void testRetrieveMovieByYear() {
        //Given
        Integer movieYear = 2012;

        //When
        List<Movie> movies = moviesRestClient.retrieveMoviesByYear(movieYear);

        //Then
        assertEquals(2, movies.size());
    }

    @Test
    void testRetrieveMovieByYear_NotFound() {
        //Given
        Integer movieYear = 2000;

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByYear(movieYear));
    }

    @Test
    void testAddMovie() {
        Movie newMovie = new Movie(null, "Toy Story 4",
                "Tom Hanks, Tim Allen", 2019,
                LocalDate.of(2019, 06, 20));

        Movie movie = moviesRestClient.addMovie(newMovie);

        assertEquals("Toy Story 4", movie.getName());
    }

    @Test
    void testAddMovie_BadRequest(){
        Movie newMovie = new Movie(null, null,
                "Tom Hanks, Tim Allen", 2019,
                LocalDate.of(2019, 06, 20));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
    }

    @Test
    void testUpdateMovie() {
        Movie movie_4 = new Movie(null, null,
                "ABC", null, null);

        Movie updatedMovie = moviesRestClient.updateMovie(4, movie_4);

        assertTrue(updatedMovie.getCast().contains("ABC"));
    }

    @Test
    void testUpdateMovie_NotFound() {
        Movie movie_100 = new Movie(null, null,
                "ABC", null, null);

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(100, movie_100));
    }

    @Test
    void testDeleteMovie() {

        //Given
        Movie newMovie = new Movie(null, "Toy Story 4",
                "Tom Hanks, Tim Allen", 2019,
                LocalDate.of(2019, 06, 20));

        Movie movie = moviesRestClient.addMovie(newMovie);

        //When
        String responseMessage = moviesRestClient.deleteMovie(movie.getMovie_id().intValue());

        //Then
        assertEquals("Movie Deleted Successfully", responseMessage);
    }

    @Test
    void testDeleteMovie_NotFound() {
        Integer movieId = 100;

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

}
