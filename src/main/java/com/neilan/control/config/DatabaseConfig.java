package com.neilan.control.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres")) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(toJdbcUrl(databaseUrl));
            config.setUsername(extractUsername(databaseUrl));
            config.setPassword(extractPassword(databaseUrl));
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(30000);
            return new HikariDataSource(config);
        }

        return properties.initializeDataSourceBuilder().build();
    }

    private String toJdbcUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String dbName = uri.getPath().replaceFirst("/", "");
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String sslMode = host.contains("railway.internal") ? "disable" : "require";
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + sslMode;
    }

    private String extractUsername(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            return userInfo.split(":")[0];
        }
        return userInfo;
    }

    private String extractPassword(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            return userInfo.split(":", 2)[1];
        }
        return "";
    }
}
