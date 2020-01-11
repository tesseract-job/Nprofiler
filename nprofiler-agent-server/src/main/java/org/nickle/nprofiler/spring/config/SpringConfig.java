package org.nickle.nprofiler.spring.config;

import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJmapServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean
    public IJavaProcessService javaProcessService() {
        return new DefaultJavaProcessServiceImpl();
    }

    @Bean
    public IJmapService jmapService() {
        return new DefaultJmapServiceImpl();
    }


}
