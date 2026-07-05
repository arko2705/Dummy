package com.Dummy.demo.monitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;// an interface
import com.Dummy.demo.monitoring.interceptor.MetricsInterceptor;

@Configuration // a setup class that has to run at Startup
public class WebConfig implements WebMvcConfigurer {
    private final MetricsInterceptor metricsInterceptor;

    public WebConfig(MetricsInterceptor metricsInterceptor) {
        this.metricsInterceptor = metricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                // Observability endpoints must stay up during simulated outages so ML/monitoring
                // can still read system state (otherwise GET /api/metrics returns 500 SYSTEM_DOWN).
                .excludePathPatterns(
                        "/api/metrics",
                        "/api/metrics/**",
                        "/api/errors",
                        "/api/test",
                        "/api/hostMetrics",
                        "/api/networkMetrics",
                        "/api/appMetrics",
                        "/api/externalDependencyMetrics");
    }
}
