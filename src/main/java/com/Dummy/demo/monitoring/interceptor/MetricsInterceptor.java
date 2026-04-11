package com.Dummy.demo.monitoring.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.Dummy.demo.monitoring.service.MetricsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricsService metricsService;

    public MetricsInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
    	System.out.println("Interceptor hit: " + request.getRequestURI());
        long startTime = (long) request.getAttribute("startTime");
        long latency = System.currentTimeMillis() - startTime;

        String endpoint = request.getRequestURI();
        int statusCode = response.getStatus();

        boolean isError = statusCode >= 500 || ex != null;

        metricsService.recordRequest(endpoint, latency, statusCode, isError);
    }
}