package com.samupert.news.dto.api;

import java.util.List;

public record ApiNewsListDto(String status, Integer totalResults, List<ApiNewsDto> results, String nextPage) {}