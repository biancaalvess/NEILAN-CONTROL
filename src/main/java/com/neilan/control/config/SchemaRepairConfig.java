package com.neilan.control.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Garante colunas novas em bancos que já existiam antes do ddl-auto atualizar.
 * Evita 500 no /api/dashboard quando falta custo_insumos.
 */
@Configuration
@Profile("prod")
public class SchemaRepairConfig {

    private static final Logger log = LoggerFactory.getLogger(SchemaRepairConfig.class);

    @Bean
    ApplicationRunner ensureCustoInsumosColumn(JdbcTemplate jdbc) {
        return args -> {
            Integer exists = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = 'servicos_realizados'
                      AND column_name = 'custo_insumos'
                    """, Integer.class);
            if (exists != null && exists > 0) {
                return;
            }
            log.warn("Coluna custo_insumos ausente — aplicando ALTER TABLE");
            jdbc.execute("""
                    ALTER TABLE servicos_realizados
                    ADD COLUMN custo_insumos numeric(10,2) NOT NULL DEFAULT 0
                    """);
            log.info("Coluna custo_insumos criada");
        };
    }
}
