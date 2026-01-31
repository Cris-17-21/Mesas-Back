package com.restaurante.resturante.mapper.security;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.UserDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDtoMapper {
    /**
     * Convierte una entidad User a su DTO de respuesta para el login. Este DTO
     * contiene toda la información inicial que necesita el frontend.
     */
    public UserDto toUserDto(User user) {
        // 3. Construimos y devolvemos el UserDto final.
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getNombres(),
                user.getApellidoPaterno(),
                user.getApellidoMaterno(),
                user.getTipoDocumento() != null ? user.getTipoDocumento().getName() : null,
                user.getNumeroDocumento(),
                user.getTelefono(),
                user.getEmail(),
                user.getRole().getName()
        );
    }

    public User toEntity(CreateUserDto dto, Role rol, TipoDocumento tipoDoc) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setNombres(dto.nombres());
        user.setApellidoPaterno(dto.apellidoPaterno());
        user.setApellidoMaterno(dto.apellidoMaterno());
        user.setNumeroDocumento(dto.numeroDocumento());
        user.setTelefono(dto.telefono());
        user.setEmail(dto.email());
        user.setRole(rol);
        user.setTipoDocumento(tipoDoc);

        return user;
    }
}
