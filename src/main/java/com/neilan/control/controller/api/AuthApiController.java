package com.neilan.control.controller.api;

import com.neilan.control.dto.UsuarioDto;
import com.neilan.control.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UsuarioRepository usuarioRepository;

    public AuthApiController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDto> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return usuarioRepository.findByEmailIgnoreCase(user.getUsername())
                .map(u -> ResponseEntity.ok(new UsuarioDto(u.getEmail(), u.getNome())))
                .orElse(ResponseEntity.status(401).build());
    }
}
