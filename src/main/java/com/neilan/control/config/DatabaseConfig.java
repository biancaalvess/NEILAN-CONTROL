package com.neilan.control.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${SPRING_DATASOURCE_URL:${DATABASE_URL:}}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        // Se a URL começar com "jdbc:", limpa o prefixo para tratar o URI
        String cleanUrl = databaseUrl.startsWith("jdbc:") ? databaseUrl.substring(5) : databaseUrl;
        
        URI dbUri = new URI(cleanUrl);

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        
        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();
        
        if (dbUri.getQuery() != null) {
            dbUrl += "?" + dbUri.getQuery();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}