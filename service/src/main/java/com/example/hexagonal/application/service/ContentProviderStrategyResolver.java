package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.out.ContentProviderStrategyPort;
import com.example.hexagonal.domain.model.ContentProviderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContentProviderStrategyResolver {

    private final Map<ContentProviderType, ContentProviderStrategyPort> strategies;

    public ContentProviderStrategyResolver(List<ContentProviderStrategyPort> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                ContentProviderStrategyPort::getProviderType,
                Function.identity()
            ));
    }

    public ContentProviderStrategyPort resolve(ContentProviderType providerType) {
        return strategies.get(providerType);
    }
}
