package com.samupert.news.mapper;

import com.samupert.news.dto.NewsDto;
import com.samupert.news.dto.NewsListDto;
import com.samupert.news.dto.api.ApiNewsListDto;

import java.util.List;

public class ApiToResponseNewsMapper {
    private static ApiToResponseNewsMapper instance = null;

    private ApiToResponseNewsMapper(){}

    public static ApiToResponseNewsMapper getInstance(){
        if(instance == null){
            instance = new ApiToResponseNewsMapper();
        }
        return instance;
    }

    public NewsListDto map(ApiNewsListDto apiNewsListDto){
        List<NewsDto> newsDtos = apiNewsListDto.results().stream().map(
                apiNewsDto -> new NewsDto(
                        apiNewsDto.title(),
                        apiNewsDto.link(),
                        apiNewsDto.description(),
                        apiNewsDto.pubDate(),
                        apiNewsDto.image_url()
                )
        ).toList();

        return new NewsListDto(
                newsDtos,
                newsDtos.size()
        );
    }
}
