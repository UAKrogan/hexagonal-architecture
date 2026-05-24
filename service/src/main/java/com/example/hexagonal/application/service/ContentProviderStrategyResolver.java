package com.example.hexagonal.application.service;

import com.example.hexagonal.domain.model.ContentProviderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContentProviderStrategyResolver {

    private final Map<ContentProviderType, ContentProviderStrategy> strategies;

    public ContentProviderStrategyResolver(List<ContentProviderStrategy> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                ContentProviderStrategy::getProviderType,
                Function.identity()
            ));
    }

    public ContentProviderStrategy resolve(ContentProviderType providerType) {
        return strategies.get(providerType);
    }
}
