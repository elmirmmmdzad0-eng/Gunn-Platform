package com.tripgen.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "tripgen-database-url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();

        String springDatasourceUrl = clean(System.getenv("SPRING_DATASOURCE_URL"));
        if (springDatasourceUrl != null) {
            properties.put("spring.datasource.url", springDatasourceUrl);
        } else {
            String databaseUrl = clean(System.getenv("DATABASE_URL"));
            if (databaseUrl != null) {
                applyDatabaseUrl(databaseUrl, properties);
            }
        }

        String effectiveUrl = (String) properties.get("spring.datasource.url");
        if (effectiveUrl == null) {
            effectiveUrl = clean(environment.getProperty("spring.datasource.url"));
        }

        if (isPostgresUrl(effectiveUrl)) {
            properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            properties.put("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");
        } else if (effectiveUrl != null && effectiveUrl.startsWith("jdbc:h2:")) {
            properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
            properties.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private void applyDatabaseUrl(String databaseUrl, Map<String, Object> properties) {
        if (databaseUrl.startsWith("jdbc:")) {
            properties.put("spring.datasource.url", databaseUrl);
            return;
        }

        if (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://")) {
            return;
        }

        URI uri = URI.create(databaseUrl);
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }

        jdbcUrl.append(uri.getPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append("?").append(uri.getRawQuery());
        }

        properties.put("spring.datasource.url", jdbcUrl.toString());

        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return;
        }

        String[] credentials = userInfo.split(":", 2);
        if (clean(System.getenv("SPRING_DATASOURCE_USERNAME")) == null
                && clean(System.getenv("DATABASE_USERNAME")) == null) {
            properties.put("spring.datasource.username", decode(credentials[0]));
        }

        if (credentials.length > 1
                && clean(System.getenv("SPRING_DATASOURCE_PASSWORD")) == null
                && clean(System.getenv("DATABASE_PASSWORD")) == null) {
            properties.put("spring.datasource.password", decode(credentials[1]));
        }
    }

    private boolean isPostgresUrl(String value) {
        return value != null
                && (value.startsWith("jdbc:postgresql:")
                || value.startsWith("postgres://")
                || value.startsWith("postgresql://"));
    }

    private String clean(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
