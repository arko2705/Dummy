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
        registry.addInterceptor(metricsInterceptor);
    }
}
