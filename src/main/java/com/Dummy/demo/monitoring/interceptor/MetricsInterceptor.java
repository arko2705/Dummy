package com.Dummy.demo.monitoring.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor; //Interface like WebMvcConfigurer

import com.Dummy.demo.monitoring.depSimulation.loadSimulator;
import com.Dummy.demo.monitoring.service.RequestMetricsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // manage this class as a bean. Go to bottom to see whats happening
public class MetricsInterceptor implements HandlerInterceptor {

    private final RequestMetricsService metricsService;

    public MetricsInterceptor(RequestMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Autowired
    private loadSimulator loadSimulator;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {// HandlerInterceptor Interface's preHandle requires all three params.
        // System.currentTimeMillis() returns the current time in milliseconds since
        // January 1, 1970 UTC
        loadSimulator.incrementLoad();
        request.setAttribute("startTime", System.currentTimeMillis());// Stores data on the request object (like a
                                                                      // temporary sticky note) that survives until the
                                                                      // response is sent. Other methods
                                                                      // (afterCompletion) can retrieve it with
                                                                      // getAttribute("startTime").

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        loadSimulator.decrementLoad();
        String endpoint = request.getRequestURI();
        int statusCode = response.getStatus();
        System.out.println("Interceptor hit: " + endpoint);// getRequestURI returns no domain(localhost:8080),no
                                                           // query(?id=123),just the endpoint
        long startTime = (long) request.getAttribute("startTime");// getAttribute() returns object
        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;

        boolean isError = statusCode >= 500 || ex != null; // treats server errors(5xx)as errors, and not client
                                                           // error (4xx) as not errors.Means sm went wrong w VALID
                                                           // request

        if (endpoint.startsWith("/api")) { // put the condition else spring internally sending random reqs(favicon,etc)
                                           // making the req count go up even tho 1 req sent
            metricsService.recordRequest(startTime, endTime, endpoint, latency, statusCode, isError);

        }
    }
}
// what if i DONT annotate metricsInterceptor with @component
// 1.BeanDefinition never created for MetricsInterceptor

// 2.No instance by Spring

// 3.Injection fails — when Spring tries to create WebConfig, it looks for a
// MetricsInterceptor bean to inject, finds nothing → throws an error (usually
// NoSuchBeanDefinitionException)