package com.rajesh.wiremock.constants;

import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

public class MoviesAppConstants {
    public static final String GET_ALL_MOVIES_V1 = "/movieservice/v1/allMovies";
    public static final String GET_MOVIE_BY_ID = "/movieservice/v1/movie/{id}";
    public static final String GET_MOVIES_BY_NAME = "/movieservice/v1/movieName";
    public static final String GET_MOVIES_BY_YEAR = "/movieservice/v1/movieYear";
    public static final String ADD_MOVIE_V1 = "/movieservice/v1/movie";
    public static final String UPDATE_MOVIE_V1 = "/movieservice/v1/movie/{id}";
    public static final String DELETE_MOVIE_BY_ID_V1 = "/movieservice/v1/movie/{id}";
}
