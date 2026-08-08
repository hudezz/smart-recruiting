package com.recruitment.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Explicitly provides the RestClient.Builder bean that PythonMlClient depends
 * on. Spring Boot normally auto-configures this from spring-web on the
 * classpath, but this project's spring-boot-starter-webmvc dependency doesn't
 * pull that auto-configuration in on its own — without this bean, the whole
 * application context fails to start.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
