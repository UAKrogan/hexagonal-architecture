package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.out.ContentProviderStrategyPort;
import com.example.hexagonal.domain.model.ContentProviderType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentProviderStrategyResolver {

    private final Map<ContentProviderType, ContentProviderStrategyPort> strategies;

    public ContentProviderStrategyResolver(List<ContentProviderStrategyPort> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                ContentProviderStrategyPort::getProviderType,
                strategy -> strategy
            ));
        verifyAllProviderTypesAreRegistered();
    }

    public ContentProviderStrategyPort resolve(ContentProviderType providerType) {
        ContentProviderStrategyPort strategy = strategies.get(providerType);
        if (strategy == null) {
            throw new IllegalStateException("No content provider strategy registered for provider: " + providerType);
        }
        return strategy;
    }

    private void verifyAllProviderTypesAreRegistered() {
        EnumSet<ContentProviderType> missingProviderTypes = EnumSet.allOf(ContentProviderType.class);
        missingProviderTypes.removeAll(strategies.keySet());

        if (!missingProviderTypes.isEmpty()) {
            throw new IllegalStateException("Missing content provider strategies: " + missingProviderTypes);
        }
    }
}
