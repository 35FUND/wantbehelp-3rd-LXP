package com.example.shortudy.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        sortResolver.setSortParameter("sort");
        sortResolver.setFallbackSort(Sort.by(Sort.Direction.ASC, "id"));

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver(sortResolver);
        pageableResolver.setFallbackPageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")));
        pageableResolver.setMaxPageSize(100);

        resolvers.add(pageableResolver);
    }
}

