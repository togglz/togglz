package org.togglz.core.repository.file;

import java.io.File;
import java.util.List;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.file.ReloadablePropertiesFile.Editor;
import org.togglz.core.util.Strings;

/**
 * 
 * <p>
 * This implementation of {@link StateRepository} stores the state of feature using a standard Java properties file.
 * </p>
 * <p>
 * The file format has changed since version 2.0.0 because of the new extendable activation strategy support. Old file formats
 * will be automatically migrated. The new format looks like this:
 * </p>
 * 
 * <pre>
 * FEATURE_ONE = true
 * FEATURE_ONE.strategy = gradual
 * FEATURE_ONE.param.percentage = 25
 * FEATURE_TWO = false
 * </pre>
 * 
 * <p>
 * Please note that this class is able to detect changes made to the properties file and will automatically reload it in this
 * case.
 * </p>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FileBasedStateRepository implements StateRepository {

    private final Log log = LogFactory.getLog(FileBasedStateRepository.class);

    private ReloadablePropertiesFile fileContent;

    /**
     * Constructor for {@link FileBasedStateRepository}.
     * 
     * @param file A {@link File} representing the Java properties file to use.
     */
    public FileBasedStateRepository(File file) {
        this.fileContent = new ReloadablePropertiesFile(file);
        log.debug(this.getClass().getSimpleName() + " initialized with: " + file.getAbsolutePath());
    }

    public FeatureState getFeatureState(Feature feature) {

        // update file if changed
        fileContent.reloadIfUpdated();

        // if we got this one, the feature is present in the repository
        String enabledAsStr = fileContent.getValue(getEnabledPropertyName(feature), null);

        if (enabledAsStr != null) {

            // new state instance
            FeatureState state = new FeatureState(feature);
            state.setEnabled(isTrue(enabledAsStr));

            // active strategy (may be null)
            String strategy = fileContent.getValue(getStrategyPropertyName(feature), null);
            state.setStrategyId(strategy);

            // all parameters
            String paramPrefix = getParameterPropertyName(feature, "");
            for (String key : fileContent.getKeysStartingWith(paramPrefix)) {
                String id = key.substring(paramPrefix.length());
                String value = fileContent.getValue(key, null);
                state.setParameter(id, value);
            }

            /*
             * Backwards compatibility: if there are users stored in the old format, add them to the corresponding property
             */
            List<String> additionalUsers = toList(fileContent.getValue(getUsersPropertyName(feature), null));
            if (!additionalUsers.isEmpty()) {

                // join the users to one list and update the property
                List<String> currentUsers = toList(state.getParameter(UsernameActivationStrategy.PARAM_USERS));
                currentUsers.addAll(additionalUsers);
                state.setParameter(UsernameActivationStrategy.PARAM_USERS, Strings.join(currentUsers, ","));

                // we should set strategy id if it is not yet set
                if (state.getStrategyId() == null) {
                    state.setStrategyId(UsernameActivationStrategy.ID);
                }

            }

            return state;

        }

        // the feature is not configured in the repository
        return null;

    }

    public void setFeatureState(FeatureState featureState) {

        // update file if changed
        fileContent.reloadIfUpdated();

        Feature feature = featureState.getFeature();
        Editor editor = fileContent.getEditor();

        // enabled
        String enabledKey = getEnabledPropertyName(feature);
        String enabledValue = featureState.isEnabled() ? "true" : "false";
        editor.setValue(enabledKey, enabledValue);

        // write strategy id, will be removed if it is null
        editor.setValue(getStrategyPropertyName(feature), featureState.getStrategyId());

        // parameters
        String paramPrefix = getParameterPropertyName(feature, "");
        editor.removeKeysStartingWith(paramPrefix);
        for (String id : featureState.getParameterNames()) {
            String key = getParameterPropertyName(feature, id);
            editor.setValue(key, featureState.getParameter(id));
        }

        // remove the old users property if it still exists from the old format
        editor.setValue(getUsersPropertyName(feature), null);

        // write
        editor.commit();

    }

    private static String getEnabledPropertyName(Feature feature) {
        return feature.name();
    }

    private static String getStrategyPropertyName(Feature feature) {
        return feature.name() + ".strategy";
    }

    private static String getParameterPropertyName(Feature feature, String parameter) {
        return feature.name() + ".param." + parameter;
    }

    private static String getUsersPropertyName(Feature feature) {
        return feature.name() + ".users";
    }

    private static boolean isTrue(String s) {
        return s != null
            && ("true".equalsIgnoreCase(s.trim()) || "yes".equalsIgnoreCase(s.trim())
                || "enabled".equalsIgnoreCase(s.trim()) || "enable".equalsIgnoreCase(s.trim()));
    }

    private static List<String> toList(String input) {
        return Strings.splitAndTrim(input, ",");
    }

}
