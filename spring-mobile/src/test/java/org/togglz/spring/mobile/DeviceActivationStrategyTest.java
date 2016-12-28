package org.togglz.spring.mobile;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceType;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.togglz.spring.mobile.DeviceActivationStrategyTest.MockRequest.requestFrom;
import static org.togglz.spring.mobile.DeviceActivationStrategyTest.MockRequestAssert.assertThat;
import static org.springframework.mobile.device.DeviceType.MOBILE;
import static org.springframework.mobile.device.DeviceType.NORMAL;
import static org.springframework.mobile.device.DeviceType.TABLET;



/**
 * Created by achhabra on 10/17/16.
 */
public class DeviceActivationStrategyTest {

    protected static class MockRequest {
        private final HttpServletRequest request;

        public static MockRequest requestFrom(DeviceType deviceType) {
            Device device = mock(Device.class);
            when(device.isNormal()).thenReturn(NORMAL.equals(deviceType));
            when(device.isTablet()).thenReturn(TABLET.equals(deviceType));
            when(device.isMobile()).thenReturn(MOBILE.equals(deviceType));
            return new MockRequest(device);
        }

        private MockRequest(Device device) {
            request = mock(HttpServletRequest.class);
            when(request.getAttribute("currentDevice")).thenReturn(device);
            HttpServletRequestHolder.bind(request);
        }
        
    }

    protected static class MockRequestAssert extends org.assertj.core.api.AbstractAssert<MockRequestAssert, MockRequest> {
        protected MockRequestAssert(MockRequest actual) {
            super(actual, MockRequestAssert.class);
        }

        public static MockRequestAssert assertThat(MockRequest actual) {
            return new MockRequestAssert(actual);
        }

        public MockRequestAssert isActiveWithParams(String... params) {
            if (!strategy().isActive(featureState(params), null)) {
                Assertions.fail("Expected the strategy to turn the feature active with params " + params);
            }
            return this;
        }

        public MockRequestAssert isInactiveWithParams(String... params) {
            if (strategy().isActive(featureState(params), null)) {
                Assertions.fail("Expected the strategy to turn the feature inactive with params " + params);
            }
            return this;
        }

        private static DeviceActivationStrategy strategy() {
            return new DeviceActivationStrategy();
        }


        private static FeatureState featureState(String... ips) {
            return new FeatureState(TestFeature.TEST_FEATURE)
                    .enable()
                    .setStrategyId(DeviceActivationStrategy.ID)
                    .setParameter(NORMAL.name(), ips[0])
                    .setParameter(TABLET.name(), ips[1])
                    .setParameter(MOBILE.name(), ips[2]);
        }

        private enum TestFeature implements Feature {
            TEST_FEATURE
        }
    }

    public void cleanup() {
        HttpServletRequestHolder.release();
    }

    @Test
    public void shouldBeInactiveForEmptyParams() throws Exception {
        String[] emptyArguments = new String[]{"", "", ""};
        assertThat(requestFrom(NORMAL)).isInactiveWithParams(emptyArguments);
        cleanup();
        assertThat(requestFrom(TABLET)).isInactiveWithParams(emptyArguments);
        cleanup();
        assertThat(requestFrom(MOBILE)).isInactiveWithParams(emptyArguments);
        cleanup();
    }

    @Test
    public void shouldBeActiveForDesktop() throws Exception {
        String[] desktopOn = new String[]{"YES", "NO", "NO"};
        assertThat(requestFrom(NORMAL)).isActiveWithParams(desktopOn);
        cleanup();
        assertThat(requestFrom(TABLET)).isInactiveWithParams(desktopOn);
        cleanup();
        assertThat(requestFrom(MOBILE)).isInactiveWithParams(desktopOn);
        cleanup();
    }

    @Test
    public void shouldBeActiveForTablet() throws Exception {
        String[] tabletOn = new String[]{"NO", "YES", "NO"};
        assertThat(requestFrom(NORMAL)).isInactiveWithParams(tabletOn);
        cleanup();
        assertThat(requestFrom(TABLET)).isActiveWithParams(tabletOn);
        cleanup();
        assertThat(requestFrom(MOBILE)).isInactiveWithParams(tabletOn);
        cleanup();
    }

    @Test
    public void shouldBeActiveForMobile() throws Exception {
        String[] mobileOn = new String[]{"NO", "NO", "YES"};
        assertThat(requestFrom(NORMAL)).isInactiveWithParams(mobileOn);
        cleanup();
        assertThat(requestFrom(TABLET)).isInactiveWithParams(mobileOn);
        cleanup();
        assertThat(requestFrom(MOBILE)).isActiveWithParams(mobileOn);
        cleanup();
    }

    @Test
    public void shouldBeAccurateForLowerCaseParams() throws Exception {
        String[] desktopOn = new String[]{"yes", "no", "yes"};
        assertThat(requestFrom(NORMAL)).isActiveWithParams(desktopOn);
        cleanup();
        String[] tabletOn = new String[]{"no", "yes", "NO"};
        assertThat(requestFrom(TABLET)).isActiveWithParams(tabletOn);
        cleanup();
        String[] mobileOn = new String[]{"no", "no", "yes"};
        assertThat(requestFrom(MOBILE)).isActiveWithParams(mobileOn);
        cleanup();
    }
}
