package org.togglz.servlet.activation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.togglz.servlet.activation.ClientIpActivationStrategyTest.MockRequest.requestFrom;
import static org.togglz.servlet.activation.ClientIpActivationStrategyTest.MockRequestAssert.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.servlet.activation.ClientIpActivationStrategy.AddressParameter;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;

public class ClientIpActivationStrategyTest {

   protected static class MockRequest {
      private final HttpServletRequest request;
      
      public static MockRequest requestFrom(String remoteAddr) {
         return new MockRequest(remoteAddr);
      }
      
      private MockRequest(String remoteAddr) {
         request = mock(HttpServletRequest.class);
         when(request.getRemoteAddr()).thenReturn(remoteAddr);
         HttpServletRequestHolder.bind(request);
      }
      
      public HttpServletRequest getRequest() {
         return request;
      }
   }

   protected static class MockRequestAssert extends org.assertj.core.api.AbstractAssert<MockRequestAssert, MockRequest> {
      protected MockRequestAssert(MockRequest actual) {
         super(actual, MockRequestAssert.class);
      }
      
      public static MockRequestAssert assertThat(MockRequest actual) {
         return new MockRequestAssert(actual);
      }
      
      public MockRequestAssert isActiveWithParams(String params) {
         if (!strategy().isActive(featureState(params), null)) {
            Assertions.fail("Expected the strategy to turn the feature active with params " + params);
         }
         return this;
      }

      public MockRequestAssert isInactiveWithParams(String params) {
         if (strategy().isActive(featureState(params), null)) {
            Assertions.fail("Expected the strategy to turn the feature inactive with params " + params);
         }
         return this;
      }

      private static ClientIpActivationStrategy strategy() {
          return new ClientIpActivationStrategy();
      }
      

      private static FeatureState featureState(String ips) {
          return new FeatureState(TestFeature.TEST_FEATURE)
              .enable()
              .setStrategyId(ClientIpActivationStrategy.ID)
              .setParameter(ClientIpActivationStrategy.PARAM_IPS, ips);
      }

      private enum TestFeature implements Feature {
          TEST_FEATURE
      }
   }
   
    @AfterEach
    public void cleanup() {
        HttpServletRequestHolder.release();
    }

    @Test
    public void shouldBeInactiveForNullParams() throws Exception {
       assertThat(requestFrom("10.1.2.3")).isInactiveWithParams(null);
    }
    
    @Test
    public void shouldBeInactiveForEmptyParams() throws Exception {
       assertThat(requestFrom("10.1.2.3")).isInactiveWithParams("");
    }

    @Test
    public void shouldBeInactiveForNonMatchingIp() throws Exception {
        assertThat(requestFrom("10.1.2.3")).isInactiveWithParams("10.1.2.4");
    }

    @Test
    public void shouldBeActiveForFirstMatchingIp() throws Exception {
        assertThat(requestFrom("192.168.0.1")).isActiveWithParams("192.168.0.1,10.1.2.3");
    }

    @Test
    public void shouldBeActiveForSecondMatchingIp() throws Exception {
        assertThat(requestFrom("10.1.2.3")).isActiveWithParams("192.168.0.1,10.1.2.3");
    }

    @Test
    public void shouldBeInactiveForNonMatchingRange() throws Exception {
        assertThat(requestFrom("10.1.2.16")).isInactiveWithParams("192.168.0.1,10.1.2.0/28");
    }

    @Test
    public void shouldBeActiveForFirstMatchingRange() throws Exception {
        assertThat(requestFrom("192.168.0.5")).isActiveWithParams("192.168.0.0/24,10.1.2.0/24");
    }

    @Test
    public void shouldBeActiveForSecondMatchingRange() throws Exception {
        assertThat(requestFrom("10.1.2.3")).isActiveWithParams("192.168.0.0/24,10.1.2.0/24");
    }

    @Test
    public void shouldBeInactiveForInvalidCidrNotation() throws Exception {
        assertThat(requestFrom("10.1.2.3")).isInactiveWithParams("192.168.0.0/24,abc/24");
    }

    @Test
    public void shouldBeInactiveForNonMatchingIpv6() throws Exception {
       assertThat(requestFrom("2001:db8:0:0:0:0:0:1")).isInactiveWithParams("2001:db8:0:0:0:0:0:2");
    }
    
    @Test
    public void shouldBeActiveForMatchingIpv6() throws Exception {
       assertThat(requestFrom("2001:db8:0:0:0:0:0:1")).isActiveWithParams("2001:db8:0:0:0:0:0:1");
    }

    @Test
    public void shouldBeInactiveForNonMatchingIpv6ShortForm() throws Exception {
       assertThat(requestFrom("2001:db8:0:0:0:0:0:1")).isInactiveWithParams("2001:db8::2");
    }
    
    @Test
    public void shouldBeActiveForMatchingIpv6ShortForm() throws Exception {
       assertThat(requestFrom("2001:db8:0:0:0:0:0:1")).isActiveWithParams("2001:db8::1");
    }

    @Test
    public void shouldBeActiveForMatchingIpv6Range() throws Exception {
       assertThat(requestFrom("2001:db8:0:0:0:0:0:1")).isActiveWithParams("2001:db8::/24");
    }
    
    @Test
    public void addressParameterProperties() {
       AddressParameter param = addressParam();
       assertThat(param.getDescription()).isNotEmpty();
       assertThat(param.getLabel()).isNotEmpty();
       assertThat(param.getName()).isEqualTo(ClientIpActivationStrategy.PARAM_IPS);
       assertThat(param.isLargeText()).isFalse();
       assertThat(param.isOptional()).isFalse();
    }

    @Test
    public void addressParameterShouldBeInvalidWithEmptyInput() {
       assertFalse(addressParam().isValid(null));
       assertFalse(addressParam().isValid(""));
    }

    @Test
    public void addressParameterShouldBeInvalidWithWrongCidrFormat() {
       assertFalse(addressParam().isValid("some-invalid-string/24"));
    }

    @Test
    public void addressParameterShouldBeValidWithIpv4Address() {
       assertTrue(addressParam().isValid("10.1.2.3"));
    }

    @Test
    public void addressParameterShouldBeInvalidWithWrongIpv4Address() {
       assertFalse(addressParam().isValid("1.2....."));
    }

    @Test
    public void addressParameterShouldBeInvalidWithWrongIpv6Address() {
       assertFalse(addressParam().isValid("[2001:db8::1"));
       assertFalse(addressParam().isValid("2001:db8:::1"));
    }

    @Test
    public void addressParameterShouldBeValidWithIpv4AddressRange() {
       assertTrue(addressParam().isValid("10.1.2.0/24"));
    }

    @Test
    public void addressParameterShouldBeValidWithIpv6Address() {
       assertTrue(addressParam().isValid("2001:db8:0:0:0:0:0:1"));
       assertTrue(addressParam().isValid("2001:db8::1"));
    }

    @Test
    public void addressParameterShouldBeValidWithIpv6AddressRange() {
       assertTrue(addressParam().isValid("2001:db8:0:0:0:0:0:0/24"));
       assertTrue(addressParam().isValid("2001:db8::/24"));
    }

    private AddressParameter addressParam() {
       return new AddressParameter();
    }
}
