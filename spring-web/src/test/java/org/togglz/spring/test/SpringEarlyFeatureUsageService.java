package org.togglz.spring.test;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

@Component
@Lazy  // Fixes the early feature usage problem
@Scope("singleton")
public class SpringEarlyFeatureUsageService {

    private boolean feature1Active;

    private boolean feature2Active;

    @PostConstruct
    public void init() {
        FeatureManager featureManager = FeatureContext.getFeatureManager();
        feature1Active = featureManager.isActive(BasicFeatures.FEATURE1);
        feature2Active = featureManager.isActive(BasicFeatures.FEATURE1);
    }

    public boolean isFeature1Active() {
        return feature1Active;
    }

    public boolean isFeature2Active() {
        return feature2Active;
    }

}
