package com.samupert.http;

import com.samupert.http.errors.RequestFailedException;

import java.net.URL;

public interface IHttpService {

    /**
     * Makes a GET request to the provided url and returns the response body.
     *
     * @param url The url to fetch.
     * @param classOfT The class of the type to cast the response to.
     * @return The response, cast to type T.
     * @param <T> The type to cast the response to.
     * @throws RequestFailedException Thrown when the request fails.
     */
    <T> T getPageContent(URL url, Class<T> classOfT) throws RequestFailedException;
}
