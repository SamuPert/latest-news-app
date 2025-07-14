package com.samupert.news.dto;

import java.util.List;

public record NewsListDto(
        List<NewsDto> news,
        Integer results
) {
}
