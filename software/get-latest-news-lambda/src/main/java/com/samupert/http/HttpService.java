package com.samupert.http;

import com.google.gson.Gson;
import com.samupert.http.errors.RequestFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class HttpService implements IHttpService{
    @Override
    public <T> T getPageContent(URL url, Class<T> classOfT) throws RequestFailedException {

        try {
            // Set timeout to 5 seconds for connect and read operations.
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Open the HTTP input stream and read the response.
            try (
                    InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
            ) {
                Gson gson = new Gson();
                return gson.fromJson(bufferedReader.lines().collect(Collectors.joining(System.lineSeparator())), classOfT);
            }
        } catch (IOException e) {
            throw new RequestFailedException(e.getMessage());
        }
    }
}
