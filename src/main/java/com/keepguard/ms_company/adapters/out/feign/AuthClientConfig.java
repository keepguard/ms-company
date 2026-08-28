package com.keepguard.ms_company.adapters.out.feign;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthClientConfig {

    @Bean
    public Logger.Level authClientLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
