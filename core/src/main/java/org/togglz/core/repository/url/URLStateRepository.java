package org.togglz.core.repository.url;

import org.togglz.core.Feature;
import org.togglz.core.activation.URLActivationStrategy;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A very simply implementation of {@link org.togglz.core.repository.StateRepository} entirely on memory.<br/>
 * This class is typically only used for integration tests or at development time.<br/>
 * The url feature toggling is embed into DefaultFeatureManager and togglz filter.<br/>
 * <br/>
 * if you create your own feature manager and want to use the url feature toggling, you will need to implement the isActive() method of the feature/feature manager as follows:<br/>
 * <br/>
 * <pre>
 * <code>public boolean isActive(Feature feature) {
 *  //first check url repository
 *  if (urlStateRepository.isExists(feature)) {
 *      return urlStateRepository.isActive(feature);
 *  }
 *  //then check feature manager's feature (custom repository)
 *  return FeatureContext.getFeatureManager().isActive(feature);
 * }</code>
 * </pre>
 * example urls:<br/>
 * www.mydomain.com?togglz=FEATURE1,true --> enables FEATURE1 for all users (isActive() will return true)<br/>
 * www.mydomain.com?togglz=FEATURE1,true,user1 --> enables FEATURE1 for user1<br/>
 * www.mydomain.com?togglz=FEATURE1,true,user1,user2,user3 --> enables FEATURE1 for user1,user2,user3<br/>
 * www.mydomain.com?togglz=FEATURE1,false --> enables FEATURE1 for user1 (isActive() will return false)<br/>
 * www.mydomain.com?togglz=FEATURE1,enable --> enables FEATURE1 state<br/>
 * www.mydomain.com?togglz=FEATURE1,disable --> disables FEATURE1 state<br/>
 * www.mydomain.com?togglz=clear --> removes all url based features toggling<br/>
 * @author Eli Abramovitch
 */
public class URLStateRepository {
    public static final String ENABLE_FEATURE_TOKEN = "enable";
    public static final String DISABLE_FEATURE_TOKEN = "disable";
    public static final String CLEAR_FEATURE_TOKEN = "clear";
    private static URLStateRepository instance;
    private final Map<String, FeatureState> states = new ConcurrentHashMap<String, FeatureState>();
    private URLActivationStrategy strategy = new URLActivationStrategy();

    private URLStateRepository() {
    }

    public static URLStateRepository getInstance() {
        if (instance == null) {
            synchronized (URLStateRepository.class) {
                if (instance == null) {
                    instance = new URLStateRepository();
                }
            }
        }
        return instance;
    }

    public FeatureState getFeatureState(Feature feature) {
        return states.get(feature.name());
    }

    /**
     * parse the feature string into a feature state.<br/>
     * the string format should be %FEATURE_NAME%,%FEATURE_VALUE%,%USERS%<br/><br/>
     * %FEATURE_NAME% - the name of the feature as it appears on the feature list enum<br/>
     * %FEATURE_VALUE% - the value of the feature - true/false/remove (clear will remove the feature from the in-memory map)<br/>
     * %USERS% - the list of users that the feature applies to. it's a comma separated array i.e: user1,user2,user3<br/>
     *
     * @param featureString the feature representation as a string
     */
    public void setFeatureState(String featureString) {
        //check to remove all states
        if (featureString.equalsIgnoreCase(CLEAR_FEATURE_TOKEN)) {
            states.clear();
            return;
        }

        //parse parameters
        String[] featureValues = featureString.split(",", 3);
        if (featureValues.length == 1) {
            System.out.println("TOGGLZ: Missing Values in url parameter");
            return;
        }
        final String featureName = featureValues[0];
        final String featureValue = featureValues[1];
        if (featureValue.equalsIgnoreCase(ENABLE_FEATURE_TOKEN)) {
                //enable feature
            if (states.containsKey(featureName)) {
                states.get(featureName).enable();
            }
        } else if (featureValue.equalsIgnoreCase(DISABLE_FEATURE_TOKEN)) {
                //disable feature
            if (states.containsKey(featureName)) {
                states.get(featureName).disable();
            }
        } else {
            //update feature state
            FeatureState featureState = new FeatureState(generateFeature(featureName, featureValue));
            featureState.setStrategyId(URLActivationStrategy.ID);
            featureState.enable();

            //add users
            if (featureValues.length == 3) {
                featureState.setParameter(URLActivationStrategy.PARAM_USERS, featureValues[2]);
            }

            states.put(featureName, featureState);
        }
    }

    /**
     * generates a feature based on string values
     *
     * @param featureName  the name of the feature (i.e: MY_FEATURE_1)
     * @param featureValue the value of the feature (true/false)
     * @return the generated feature
     */
    private Feature generateFeature(final String featureName, final String featureValue) {
        final boolean isActive = Boolean.valueOf(featureValue);
        return new Feature() {
            @Override
            public String name() {
                return featureName;
            }

            @Override
            public boolean isActive() {
                return isActive;
            }
        };
    }


    public boolean isExists(Feature feature) {
        return states.containsKey(feature.name());
    }

    public Integer size() {
        return states.size();
    }

    public boolean isActive(Feature feature) {
        FeatureState featureState = states.get(feature.name());
        if (featureState.isEnabled() && featureState.getFeature().isActive()) {
            return strategy.isActive(states.get(feature.name()), FeatureContext.getFeatureManager().getCurrentFeatureUser());
        }
        return false;
    }
}