package org.togglz.spring.web.spi;

import org.springframework.web.filter.OncePerRequestFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpServletRequestHolderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
    try {
      HttpServletRequestHolder.bind(httpServletRequest);
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
    finally {
      HttpServletRequestHolder.release();
    }
  }
}
