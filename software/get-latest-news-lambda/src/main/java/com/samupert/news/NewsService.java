package com.samupert.news;

import com.samupert.http.IHttpService;
import com.samupert.http.errors.RequestFailedException;
import com.samupert.news.dto.NewsListDto;
import com.samupert.news.dto.api.ApiNewsListDto;
import com.samupert.news.mapper.ApiToResponseNewsMapper;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class NewsService implements INewsService {

    private final IHttpService httpService;
    private final String BASE_URL = "https://newsdata.io/api/1/news";
    private final String apiKey;

    public NewsService(IHttpService httpService) {
        this.httpService = httpService;
        this.apiKey = System.getenv("API_KEY");
    }

    @Override
    public NewsListDto getLatestNews() throws RequestFailedException {

        try {
            URIBuilder uriBuilder = new URIBuilder(this.BASE_URL);
            uriBuilder.addParameter("apikey", this.apiKey);
            uriBuilder.addParameter("language", "en");
            URL latestNewsUrl = uriBuilder.build().toURL();

            // Fetch the latest news
            ApiNewsListDto apiLatestNews = this.httpService.getPageContent(latestNewsUrl, ApiNewsListDto.class);
            return ApiToResponseNewsMapper.getInstance().map(apiLatestNews);

        } catch (URISyntaxException | MalformedURLException e) {
            throw new RequestFailedException("Invalid URL.");
        }
    }
}
