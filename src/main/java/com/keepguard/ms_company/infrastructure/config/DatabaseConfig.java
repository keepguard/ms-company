package com.keepguard.ms_company.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.keepguard.ms_company.domain")
public class DatabaseConfig {
}
