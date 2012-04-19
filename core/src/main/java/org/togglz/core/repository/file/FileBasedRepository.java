package org.togglz.core.repository.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.FeatureStateRepository;
import org.togglz.core.repository.file.ReloadablePropertiesFile.Editor;


/**
 * 
 * <p>
 * This implementation of {@link FeatureStateRepository} stores the state of feature using a standard Java properties file.
 * </p>
 * <p>
 * The file has the following format:
 * </p>
 * 
 * <pre>
 * FEATURE_ONE = true
 * FEATURE_ONE.users = admin, chkal
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
public class FileBasedRepository implements FeatureStateRepository {

    private final Logger log = LoggerFactory.getLogger(FileBasedRepository.class);

    private ReloadablePropertiesFile fileContent;

    /**
     * Constructor for {@link FileBasedRepository}.
     * 
     * @param file A {@link File} representing the Java properties file to use.
     */
    public FileBasedRepository(File file) {
        this.fileContent = new ReloadablePropertiesFile(file);
        log.debug(this.getClass().getSimpleName() + " initialized with: " + file.getAbsolutePath());
    }

    public FeatureState getFeatureState(Feature feature) {

        // update file if changed
        fileContent.reloadIfUpdated();

        // feature enabled?
        String enabledAsStr = fileContent.getValue(getEnabledPropertyName(feature), null);
        if (enabledAsStr != null) {
            List<String> users = toList(fileContent.getValue(getUsersPropertyName(feature), null));
            return new FeatureState(feature, isTrue(enabledAsStr), users);
        }

        return null;

    }

    public void setFeatureState(FeatureState featureState) {

        // update file if changed
        fileContent.reloadIfUpdated();

        Editor editor = fileContent.getEditor();

        // enabled
        String enabledKey = getEnabledPropertyName(featureState.getFeature());
        String enabledValue = featureState.isEnabled() ? "true" : "false";
        editor.setValue(enabledKey, enabledValue);

        // users
        String usersKey = getUsersPropertyName(featureState.getFeature());
        String usersValue = join(featureState.getUsers());
        if (usersValue.length() > 0) {
            editor.setValue(usersKey, usersValue);
        } else {
            editor.removeValue(usersKey);
        }

        // write
        editor.commit();

    }

    private static String getEnabledPropertyName(Feature feature) {
        return feature.name();
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
        List<String> result = new ArrayList<String>();
        if (input != null) {
            for (String s : input.split("\\s*,\\s*")) {
                if (s != null && s.trim().length() > 0) {
                    result.add(s.trim());
                }
            }
        }
        return result;
    }

    private static String join(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(s);
        }
        return result.toString();
    }

}
