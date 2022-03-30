package org.togglz.core.activation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

public class ReleaseDateActivationStrategy implements ActivationStrategy {

    private final Logger log = LoggerFactory.getLogger(ReleaseDateActivationStrategy.class);

    public static final String ID = "release-date";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_TIME = "time";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Release date";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {

        String dateStr = featureState.getParameter(PARAM_DATE);
        String timeStr = featureState.getParameter(PARAM_TIME);

        Date releaseDate = parseReleaseDate(dateStr, timeStr);
        if (releaseDate != null) {
            return new Date().after(releaseDate);
        }
        return false;

    }

    private Date parseReleaseDate(String dateStr, String timeStr) {

        StringBuilder fullDate = new StringBuilder();
        fullDate.append(dateStr.trim());
        fullDate.append('T');
        if (Strings.isNotBlank(timeStr)) {
            fullDate.append(timeStr.trim());
        } else {
            fullDate.append("00:00:00");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return dateFormat.parse(fullDate.toString());
        } catch (ParseException e) {
            log.error("Invalid date and/or time: " + fullDate);

        }
        return null;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
                ParameterBuilder.create(PARAM_DATE).label("Date").matching("\\d{4}\\-\\d{2}\\-\\d{2}")
                    .description("Release date of the feature. Format: 2012-12-31"),
                ParameterBuilder.create(PARAM_TIME).label("Time").matching("\\d{2}\\:\\d{2}\\:\\d{2}").optional()
                    .description("Optional time for the release day. The default value is midnight. Format: 14:45:00")
        };
    }

}
