package com.neilan.control.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = resolveDatabaseUrl();
        String defaultUsername = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_USERNAME"),
                System.getenv("DATABASE_USERNAME"),
                System.getenv("PGUSER")
        );
        String defaultPassword = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_PASSWORD"),
                System.getenv("DATABASE_PASSWORD"),
                System.getenv("PGPASSWORD")
        );

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        String cleanUrl = databaseUrl.trim();
        if (cleanUrl.startsWith("jdbc:")) {
            cleanUrl = cleanUrl.substring(5);
        }

        URI dbUri = new URI(cleanUrl);

        String username = defaultUsername;
        String password = defaultPassword;

        if (dbUri.getUserInfo() != null) {
            String[] userInfo = dbUri.getUserInfo().split(":", 2);
            if (userInfo.length > 0 && !userInfo[0].isEmpty()) {
                username = userInfo[0];
            }
            if (userInfo.length > 1 && !userInfo[1].isEmpty()) {
                password = userInfo[1];
            }
        }

        if (isBlank(username) || password == null) {
            throw new IllegalStateException(
                    "Credenciais do banco ausentes. Use DATABASE_URL com user:password@host "
                            + "ou defina SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD."
            );
        }

        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
        if (dbUri.getHost() == null || dbUri.getPath() == null || dbUri.getPath().isBlank()) {
            throw new IllegalStateException(
                    "DATABASE_URL inválida. Exemplo: postgresql://USER:PASSWORD@HOST/neondb?sslmode=require"
            );
        }

        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();

        if (dbUri.getQuery() != null) {
            String query = Arrays.stream(dbUri.getQuery().split("&"))
                    .filter(part -> !part.startsWith("channel_binding="))
                    .collect(Collectors.joining("&"));
            if (!query.isEmpty()) {
                jdbcUrl += "?" + query;
            } else {
                jdbcUrl += "?sslmode=require";
            }
        } else {
            jdbcUrl += "?sslmode=require";
        }

        config.setJdbcUrl(jdbcUrl);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(60_000);
        config.setInitializationFailTimeout(60_000);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    /**
     * Lê env direto (sem resolver placeholders aninhados do Spring).
     * Ignora SPRING_DATASOURCE_URL quebrada com ${DATABASE_HOST} do render.yaml antigo.
     */
    private static String resolveDatabaseUrl() {
        String springUrl = System.getenv("SPRING_DATASOURCE_URL");
        String databaseUrl = System.getenv("DATABASE_URL");
        String pgHost = System.getenv("PGHOST");
        String pgDb = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");
        String pgPort = firstNonBlank(System.getenv("PGPORT"), "5432");

        if (isUsableJdbcOrPostgresUrl(springUrl)) {
            return springUrl;
        }
        if (isUsableJdbcOrPostgresUrl(databaseUrl)) {
            return databaseUrl;
        }
        if (!isBlank(pgHost) && !isBlank(pgDb) && !isBlank(pgUser) && pgPassword != null) {
            return "postgresql://" + pgUser + ":" + pgPassword + "@" + pgHost + ":" + pgPort + "/" + pgDb
                    + "?sslmode=require";
        }

        throw new IllegalStateException(
                "Banco não configurado. No Render, defina DATABASE_URL "
                        + "(postgresql://USER:PASSWORD@HOST/neondb?sslmode=require) "
                        + "e remova SPRING_DATASOURCE_URL com ${DATABASE_HOST}."
        );
    }

    private static boolean isUsableJdbcOrPostgresUrl(String value) {
        if (isBlank(value) || value.contains("${")) {
            return false;
        }
        String v = value.trim();
        return v.startsWith("jdbc:postgresql://") || v.startsWith("postgresql://") || v.startsWith("postgres://");
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
