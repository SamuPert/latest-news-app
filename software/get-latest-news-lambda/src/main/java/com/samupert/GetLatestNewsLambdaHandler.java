package com.samupert;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.samupert.http.HttpService;
import com.samupert.http.IHttpService;
import com.samupert.http.errors.RequestFailedException;
import com.samupert.news.INewsService;
import com.samupert.news.NewsService;
import com.samupert.news.dto.NewsListDto;
import com.samupert.news.dto.api.ErrorResponseDto;

import java.net.HttpURLConnection;

// Handler value: com.samupert.GetLatestNewsLambdaHandler
public class GetLatestNewsLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{

    private final IHttpService httpService;
    private final INewsService newsService;

    public GetLatestNewsLambdaHandler() {
        this.httpService = new HttpService();
        this.newsService = new NewsService(this.httpService);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        final LambdaLogger logger = context.getLogger();
        final APIGatewayProxyResponseEvent apiResponseEvent = new APIGatewayProxyResponseEvent();
        final Gson gson = new Gson();

        logger.log("Handling input event: %s %s".formatted(input.getHttpMethod(), input.getPath()));

        try {

            // Fetch the latest news.
            logger.log("[INFO] Fetching the latest news...");
            NewsListDto latestNews = this.newsService.getLatestNews();
            logger.log("[INFO] %d news fetched.".formatted(latestNews.news().size()));

            apiResponseEvent.setBody(gson.toJson(latestNews));
            apiResponseEvent.setStatusCode(HttpURLConnection.HTTP_OK);
        }catch(RequestFailedException requestFailedException){

            // If the API request fails, send an error message and HTTP 502 error code.
            logger.log("[ERROR] Request failed! Message: %s".formatted(requestFailedException.getMessage()));

            ErrorResponseDto errorResponse = new ErrorResponseDto("Something went wrong with the upstream API Server. Try again later.");
            apiResponseEvent.setBody(gson.toJson(errorResponse));
            apiResponseEvent.setStatusCode(HttpURLConnection.HTTP_BAD_GATEWAY);
        }catch(Exception e){

            // If an exception is thrown, send an error message and HTTP 500 error code.
            logger.log("[ERROR] Exception thrown! Message: %s".formatted(e.getMessage()));

            ErrorResponseDto errorResponse = new ErrorResponseDto("Something went wrong.");
            apiResponseEvent.setBody(gson.toJson(errorResponse));
            apiResponseEvent.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        return apiResponseEvent;
    }
}
