package org.nickle.nprofiler.config;

import org.nickle.nprofiler.registry.IAgentRegistry;
import org.nickle.nprofiler.registry.MemoryAgentRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public IAgentRegistry agentRegistry() {
        return new MemoryAgentRegistry();
    }
}
