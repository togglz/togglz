package org.togglz.spring.mobile;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.DeviceUtils;
import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.mobile.device.DeviceType.MOBILE;
import static org.springframework.mobile.device.DeviceType.NORMAL;
import static org.springframework.mobile.device.DeviceType.TABLET;


/**
 * Activation strategy that will use the Device type used by client to decide if a feature is active or not.
 * Based on spring-mobile http://projects.spring.io/spring-mobile/
 *
 * Created by achhabra on 10/17/16.
 * @author Anupriya Chhabra
 *
 */
public class DeviceActivationStrategy implements ActivationStrategy {

    public static final String YES = "YES";
    public static final String ID = "devicerollout";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Device Rollout";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create(NORMAL.name())
                        .label("Turn this feature ON for desktop").matching("(?i)(YES|NO)")
                        .description("Feature will be off by default enter 'YES' in box above to enable"),
                ParameterBuilder.create(TABLET.name())
                        .label("Turn this feature ON for Tablet").matching("(?i)(YES|NO)")
                        .description("Feature will be off by default enter 'YES' in box above to enable"),
                ParameterBuilder.create(MOBILE.name())
                        .label("Turn this feature ON for Mobile").matching("(?i)(YES|NO)")
                        .description("Feature will be off by default enter 'YES' in box above to enable")
        };
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {

        HttpServletRequest request = HttpServletRequestHolder.get();
        if (request != null) {
            Device device = DeviceUtils.getCurrentDevice(request);
            DeviceType deviceType = device.isMobile() ? MOBILE : (device.isTablet() ? TABLET : NORMAL);
            return (YES.equalsIgnoreCase(featureState.getParameter(deviceType.name())));

        }
        return false;

    }
}
