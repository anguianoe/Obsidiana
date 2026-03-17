package com.nexcoyo.knowledge.obsidiana.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order( Ordered.LOWEST_PRECEDENCE - 10)
public class SpaForwardFilter extends HttpFilter
{

    private static final String[] IGNORED_PATH_PREFIXES = {"/api", "/actuator", "/static", "/webjars", "/public", "/resources"};
    private long serialVersionUID = 0L;

    @Override
    protected void doFilter( HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException
    {

        if (shouldForwardToIndex(req)) {
            // Forward to index.html in classpath:/static/index.html
            req.getRequestDispatcher("/index.html").forward(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean shouldForwardToIndex(HttpServletRequest request) {
        // Only handle GET requests
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();

        // Ignore root
        if ("/".equals(path) || path.isEmpty()) {
            return false; // let existing controller (or view controller) handle root if present
        }

        // Ignore known prefixes (api, actuator, static resources)
        for (String prefix : IGNORED_PATH_PREFIXES) {
            if (path.startsWith(prefix + "/") || path.equals(prefix)) {
                return false;
            }
        }

        // Ignore requests for files (contain a dot in the last path segment)
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        if (lastSegment.contains(".")) {
            return false;
        }

        return true;
    }
}

