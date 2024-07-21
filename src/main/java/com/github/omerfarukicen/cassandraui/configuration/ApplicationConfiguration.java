package com.github.omerfarukicen.cassandraui.configuration;

import com.github.omerfarukicen.cassandraui.service.BackgroundTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        return messageSource;
    }

    @Bean
    public BackgroundTaskExecutor backgroundTaskExecutor() {
        return new BackgroundTaskExecutor();
    }
}
