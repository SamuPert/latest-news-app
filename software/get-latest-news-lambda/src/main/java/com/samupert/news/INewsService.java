package com.samupert.news;

import com.samupert.http.errors.RequestFailedException;
import com.samupert.news.dto.NewsListDto;

public interface INewsService {
    /**
     * Retrieves the latest news.
     *
     * @return Returns the latest news.
     *
     * @throws RequestFailedException Thrown when the request fails.
     */
    NewsListDto getLatestNews() throws RequestFailedException;

}
