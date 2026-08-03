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

    @Value("${SPRING_DATASOURCE_USERNAME:${DATABASE_USERNAME:}}")
    private String defaultUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD:${DATABASE_PASSWORD:}}")
    private String defaultPassword;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        String cleanUrl = databaseUrl.trim();
        if (cleanUrl.startsWith("jdbc:")) {
            cleanUrl = cleanUrl.substring(5);
        }

        URI dbUri = new URI(cleanUrl);

        String username = defaultUsername;
        String password = defaultPassword;

        // Extrai credenciais da URL se existirem no formato user:pass
        if (dbUri.getUserInfo() != null) {
            String[] userInfo = dbUri.getUserInfo().split(":");
            if (userInfo.length > 0 && !userInfo[0].isEmpty()) {
                username = userInfo[0];
            }
            if (userInfo.length > 1 && !userInfo[1].isEmpty()) {
                password = userInfo[1];
            }
        }

        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();

        if (dbUri.getQuery() != null) {
            jdbcUrl += "?" + dbUri.getQuery();
        }

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }
}