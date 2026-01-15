package com.restaurante.resturante.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("auditorAware")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditorAwareImpl implements AuditorAware<String>{

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificamos si la autenticación es nula, si no está autenticado o es anónimo
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Si no hay usuario logueado (ej: en un proceso de sistema o data seeding),
            // devolvemos un valor por defecto o un Optional vacío.
            return Optional.of("SYSTEM"); 
        }

        // Devolvemos el username del usuario logueado
        return Optional.of(authentication.getName());
    }
}
