package org.togglz.spring.web.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HttpServletRequestHolderFilterTest {

  private HttpServletRequestHolderFilter filter;
  private FilterChain filterChain;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @BeforeEach
  public void setup() {
    filter = new HttpServletRequestHolderFilter();
    request = mock(HttpServletRequest.class);
    filterChain = mock(FilterChain.class);
    response = mock(HttpServletResponse.class);
  }

  @Test
  public void shouldCallFilterChain() throws ServletException, IOException {
    filter.doFilter(request, response, filterChain);
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void shouldBindCorrectRequest() throws ServletException, IOException {
    try (MockedStatic<HttpServletRequestHolder> mb = Mockito.mockStatic(HttpServletRequestHolder.class)) {
      filter.doFilter(request, response, filterChain);
      verify(HttpServletRequestHolder.class, times(1));
      HttpServletRequestHolder.bind(any());
    }
  }

  @Test
  public void shouldReleaseRequest() throws ServletException, IOException {
    try (MockedStatic<HttpServletRequestHolder> mb = Mockito.mockStatic(HttpServletRequestHolder.class)) {
      filter.doFilter(request, response, filterChain);
      verify(HttpServletRequestHolder.class, times(1));
      HttpServletRequestHolder.release();
    }
  }

  @Test
  public void shouldReleaseRequestOnExceptionWhileBinding() {
    try (MockedStatic<HttpServletRequestHolder> mb = Mockito.mockStatic(HttpServletRequestHolder.class)) {
      doThrow(new RuntimeException("boooom")).when(HttpServletRequestHolder.class);
      HttpServletRequestHolder.bind(any());
      assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));
      verify(HttpServletRequestHolder.class, times(1));
      HttpServletRequestHolder.release();
    }
  }

  @Test
  public void shouldReleaseRequestOnExceptionWhileFiltering() throws ServletException, IOException {
    try (MockedStatic<HttpServletRequestHolder> mb = Mockito.mockStatic(HttpServletRequestHolder.class)) {
      doThrow(new RuntimeException("boooom")).when(filterChain).doFilter(any(), any());
      assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));
      verify(HttpServletRequestHolder.class, times(1));
      HttpServletRequestHolder.release();
    }
  }
}
