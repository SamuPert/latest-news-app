package com.samupert.news.dto.api;

import java.util.List;

public record ApiNewsDto(
        String title,
        String link,
        List<String> keywords,
        List<String> creator,
        String video_url,
        String description,
        String content,
        String pubDate,
        String image_url,
        String source_id,
        List<String> category,
        List<String> country,
        String language
) {}