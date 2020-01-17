package org.nickle.nprofiler.spring.config;

import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.IJstatService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJmapServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJstatServiceImpl;
import org.nickle.nprofiler.registry.DefaultHttpRegistryClient;
import org.nickle.nprofiler.registry.IRegistryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Value("${nprofiler.registry.url}")
    private String remoteServerURL;

    @Bean
    public IJavaProcessService javaProcessService() {
        return new DefaultJavaProcessServiceImpl();
    }

    @Bean
    public IJmapService jmapService() {
        return new DefaultJmapServiceImpl();
    }


    @Bean
    public IJstatService jstatService() {
        return new DefaultJstatServiceImpl();
    }

    @Bean
    public IRegistryClient registryClient() {
        DefaultHttpRegistryClient defaultHttpRegistryClient = new DefaultHttpRegistryClient();
        defaultHttpRegistryClient.init(remoteServerURL, null);
        return defaultHttpRegistryClient;
    }


}
