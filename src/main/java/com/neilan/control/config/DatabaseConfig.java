package com.neilan.control.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

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

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            databaseUrl = databaseUrl.trim();
            // Railway/Neon costumam enviar postgres:// ou postgresql://
            if (databaseUrl.startsWith("postgres")) {
                HikariConfig config = buildFromPostgresUrl(databaseUrl);
                log.info("Datasource: host={} database={} sslmode=require",
                        config.getJdbcUrl().replaceAll(".*://([^:/]+).*", "$1"),
                        extractDbName(databaseUrl));
                return new HikariDataSource(config);
            }
            // Se vier jdbc:postgresql://..., deixa o Spring usar a URL direta
            if (databaseUrl.startsWith("jdbc:postgresql:")) {
                properties.setUrl(stripChannelBinding(databaseUrl));
            }
        }

        return properties.initializeDataSourceBuilder().build();
    }

    private HikariConfig buildFromPostgresUrl(String databaseUrl) {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = toJdbcUrl(databaseUrl);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(extractUsername(databaseUrl));
        config.setPassword(extractPassword(databaseUrl));
        config.setMaximumPoolSize(5);
        // Neon idle compute pode levar alguns segundos para acordar
        config.setConnectionTimeout(60_000);
        config.setInitializationFailTimeout(60_000);
        config.setMaxLifetime(300_000);
        config.setIdleTimeout(60_000);
        // PgBouncer (host -pooler) não gosta de prepared statements na sessão
        if (jdbcUrl.contains("-pooler.") || jdbcUrl.contains("-pooler")) {
            config.addDataSourceProperty("prepareThreshold", "0");
        }
        return config;
    }

    private String toJdbcUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String dbName = uri.getPath() != null ? uri.getPath().replaceFirst("/", "") : "neondb";
        if (dbName.contains("?")) {
            dbName = dbName.substring(0, dbName.indexOf('?'));
        }
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String sslMode = host.contains("railway.internal") ? "disable" : "require";
        // Sem channel_binding — driver JDBC antigo/comum reclama no Neon
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + sslMode;
    }

    private String extractDbName(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String dbName = uri.getPath() != null ? uri.getPath().replaceFirst("/", "") : "";
        if (dbName.contains("?")) {
            dbName = dbName.substring(0, dbName.indexOf('?'));
        }
        return dbName;
    }

    private String extractUsername(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            return decode(userInfo.split(":", 2)[0]);
        }
        return userInfo != null ? decode(userInfo) : null;
    }

    private String extractPassword(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            return decode(userInfo.split(":", 2)[1]);
        }
        return "";
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String stripChannelBinding(String jdbcUrl) {
        return jdbcUrl
                .replace("&channel_binding=require", "")
                .replace("?channel_binding=require&", "?")
                .replace("?channel_binding=require", "");
    }
}

