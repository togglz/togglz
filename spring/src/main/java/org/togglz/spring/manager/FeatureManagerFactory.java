package org.togglz.spring.manager;

import org.springframework.beans.factory.FactoryBean;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;

/**
 * 
 * <p>
 * {@link FactoryBean} for creating a {@link FeatureManager} managed by Spring.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * &lt;bean id="userProvider" class="org.togglz.core.user.NoOpUserProvider" /&gt;
 * 
 * &lt;bean id="stateRepository" class="org.togglz.core.repository.mem.InMemoryStateRepository" /&gt;
 * 
 * &lt;bean id="featureManager" class="org.togglz.spring.manager.FeatureManagerFactory"&gt;
 *   &lt;property name="featureEnum" value="org.example.myapp.MyFeatures" /&gt; 
 *   &lt;property name="stateRepository" ref="stateRepository" /&gt;
 *   &lt;property name="userProvider" ref="userProvider" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FeatureManagerFactory implements FactoryBean<FeatureManager> {

    private final FeatureManagerBuilder builder = new FeatureManagerBuilder();

    @Override
    public FeatureManager getObject() throws Exception {
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return FeatureManager.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setUserProvider(UserProvider userProvider) {
        builder.userProvider(userProvider);
    }

    public void setStateRepository(StateRepository stateRepository) {
        builder.stateRepository(stateRepository);
    }

    public void setFeatureEnum(Class<? extends Feature> featureEnum) {
        this.setFeatureEnums(featureEnum);
    }

    public void setFeatureEnums(Class<? extends Feature>... featureEnum) {
        builder.featureEnums(featureEnum);
    }

    public void setName(String name) {
        builder.name(name);
    }

    public void setTogglzConfig(TogglzConfig togglzConfig) {
        builder.togglzConfig(togglzConfig);
    }

    public void setFeatureProvider(FeatureProvider featureProvider) {
        builder.featureProvider(featureProvider);
    }

}
