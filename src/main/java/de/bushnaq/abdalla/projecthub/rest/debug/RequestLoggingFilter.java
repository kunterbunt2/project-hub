package de.bushnaq.abdalla.projecthub.rest.debug;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
            String                       body          = cachedRequest.getRequestBody();
            String                       requestURL    = httpRequest.getRequestURL().toString();
            System.out.format("Received JSON request: %s %s\n", requestURL, body);
            chain.doFilter(cachedRequest, response);

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final String requestBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.requestBody = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // No implementation needed
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
        }

        public String getRequestBody() {
            return this.requestBody;
        }
    }
}