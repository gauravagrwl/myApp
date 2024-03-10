package org.gauravagrwl.myApp.config;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class AppConfig {
    @Bean(name = "auditingDateTimeProvider")
	DateTimeProvider dateTimeProvider() {
		return () -> Optional.of(OffsetDateTime.now());
	}

	@Bean(name = "currentAuditor")
	AuditorAware<String> getCurrentAuditor() {
		return () -> Optional.of("home-app");
	}

}
