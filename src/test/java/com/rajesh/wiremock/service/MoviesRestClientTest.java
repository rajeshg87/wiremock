package com.rajesh.wiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.rajesh.wiremock.dto.Movie;
import com.rajesh.wiremock.exception.MovieErrorResponse;
import org.apache.commons.lang3.RandomUtils;
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

import static com.rajesh.wiremock.constants.MoviesAppConstants.*;
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
        stubFor(get(urlEqualTo(GET_ALL_MOVIES_V1))
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
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movie-id.json")));
        Integer movieId = 100;

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void testRetrieveMovieByName() {
        //Given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(GET_MOVIES_BY_NAME))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //When
        List<Movie> movies = moviesRestClient.retrieveMoviesByName(movieName);

        //Then
        assertEquals(4, movies.size());
    }

    @Test
    void testRetrieveMovieByName_ResponseTemplate() {
        //Given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(GET_MOVIES_BY_NAME+"?movie_name="+movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-by-name.json")));

        //When
        List<Movie> movies = moviesRestClient.retrieveMoviesByName(movieName);

        //Then
        assertEquals(4, movies.size());
        assertTrue(movies.get(0).getName().contains(movieName));
    }

    @Test
    void testRetrieveMovieByName_NotFound() {
        //Given
        String movieName = "ABC";

        stubFor(get(urlEqualTo(GET_MOVIES_BY_NAME+"?movie_name="+movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                )
        );

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByName(movieName));
    }


    @Test
    void testRetrieveMovieByYear() {
        //Given
        Integer movieYear = 2012;

        stubFor(get(urlEqualTo(GET_MOVIES_BY_YEAR+"?year="+movieYear))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-by-year.json")));

        //When
        List<Movie> movies = moviesRestClient.retrieveMoviesByYear(movieYear);

        //Then
        assertEquals(2, movies.size());
    }

    @Test
    void testRetrieveMovieByYear_NotFound() {
        //Given
        Integer movieYear = 2000;

        stubFor(get(urlEqualTo(GET_MOVIES_BY_YEAR+"?year="+movieYear))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                )
        );

        //When & Then
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByYear(movieYear));
    }

    @Test
    void testAddMovie() {
        //Given
        stubFor(post(urlPathMatching(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        Movie newMovie = new Movie(null, "Toy Story 4",
                "Tom Hanks, Tim Allen", 2019,
                LocalDate.of(2019, 06, 20));

        Movie movie = moviesRestClient.addMovie(newMovie);

        assertEquals("Toy Story 4", movie.getName());
    }

    @Test
    void testAddMovie_ResponseTemplate() {
        //Given
        String movieName = "Cast Away";
        stubFor(post(urlPathMatching(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo(movieName)))
                .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie-template.json")));

        Movie newMovie = new Movie(null, movieName,
                "Tom Hanks", 2019,
                LocalDate.of(2019, 06, 20));

        Movie movie = moviesRestClient.addMovie(newMovie);

        assertEquals(movieName, movie.getName());
    }

    @Test
    void testAddMovie_BadRequest(){

        stubFor(post(urlPathMatching(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("400-invalid-input.json")));

        Movie newMovie = new Movie(null, null,
                "Tom Hanks, Tim Allen", 2019,
                LocalDate.of(2019, 06, 20));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
    }

    @Test
    void testUpdateMovie() {

        //Given
        Integer movieId = 4;
        String cast = "Rajesh";
        Movie movie_4 = new Movie(null, null,
                cast, null, null);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("update-movie-template.json")));

        Movie updatedMovie = moviesRestClient.updateMovie(4, movie_4);

        assertTrue(updatedMovie.getCast().contains(cast));
    }

    @Test
    void testUpdateMovie_NotFound() {
        Movie movie_100 = new Movie(null, null,
                "ABC", null, null);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(100, movie_100));
    }

    @Test
    void testDeleteMovie() {

        Integer movieId = RandomUtils.nextInt();

        //Given
        String expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedErrorMessage)
                )
        );

        //When
        String responseMessage = moviesRestClient.deleteMovie(movieId);

        //Then
        assertEquals(expectedErrorMessage, responseMessage);
    }

    @Test
    void testDeleteMovie_NotFound() {
        Integer movieId = 100;

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                )
        );

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

}
