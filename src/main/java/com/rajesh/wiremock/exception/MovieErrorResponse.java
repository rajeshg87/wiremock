package com.rajesh.wiremock.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MovieErrorResponse extends RuntimeException{

    public MovieErrorResponse(String statusText, WebClientResponseException ex){
        super();
    }

    public MovieErrorResponse(Exception ex) {
        super();
    }
}
