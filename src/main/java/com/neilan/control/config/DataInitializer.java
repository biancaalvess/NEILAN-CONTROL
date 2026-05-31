package com.neilan.control.config;

import com.neilan.control.model.Role;
import com.neilan.control.model.TipoServico;
import com.neilan.control.model.Usuario;
import com.neilan.control.repository.TipoServicoRepository;
import com.neilan.control.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(TipoServicoRepository tipoRepo,
                               UsuarioRepository usuarioRepo,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.admin.email:neilan@estetica.com}") String adminEmail,
                               @Value("${app.admin.password:neilan1958}") String adminPassword) {
        return args -> {
            seedUsuario(usuarioRepo, passwordEncoder, adminEmail, adminPassword);
            seedServicos(tipoRepo);
        };
    }

    private void seedUsuario(UsuarioRepository repository, PasswordEncoder encoder,
                             String email, String senhaPlana) {
        if (repository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Usuario admin = new Usuario(
                email.toLowerCase(),
                encoder.encode(senhaPlana),
                "Neilan",
                Role.ADMIN
        );
        repository.save(admin);
    }

    private void seedServicos(TipoServicoRepository repository) {
        if (repository.count() > 0) {
            return;
        }

            repository.save(new TipoServico(
                    "Polimento",
                    "Remoção de riscos e marcas, deixando a pintura como nova.",
                    "Estética Automotiva",
                    new BigDecimal("150.00")
            ));
            repository.save(new TipoServico(
                    "Cristalização e Vitrificação",
                    "Proteção e brilho duradouro para a pintura do seu veículo.",
                    "Estética Automotiva",
                    new BigDecimal("200.00")
            ));
            repository.save(new TipoServico(
                    "Lavagem de Bancos e Estofados",
                    "Limpeza profunda e higienização dos estofados.",
                    "Lavagem de Estofados",
                    new BigDecimal("80.00")
            ));
            repository.save(new TipoServico(
                    "Higienização de Teto",
                    "Limpeza especializada do teto interno do veículo.",
                    "Lavagem de Estofados",
                    new BigDecimal("60.00")
            ));
            repository.save(new TipoServico(
                    "Lavagem Completa",
                    "Limpeza interna e externa do veículo.",
                    "Lavagens em Geral",
                    new BigDecimal("50.00")
            ));
            repository.save(new TipoServico(
                    "Lavagem de Motor",
                    "Limpeza especializada do compartimento do motor.",
                    "Lavagens em Geral",
                    new BigDecimal("70.00")
            ));
    }
}
