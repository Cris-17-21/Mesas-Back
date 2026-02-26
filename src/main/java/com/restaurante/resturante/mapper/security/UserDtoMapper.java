package com.restaurante.resturante.mapper.security;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.MeUserDto;
import com.restaurante.resturante.dto.security.UserDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDtoMapper {

    // Convierte de entidad a DTO (Para respuestas de API)
    public UserDto toUserDto(User user) {

        if (user == null)
            return null;

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
                user.getRole().getName());
    }

    // Convierte de CreateUserDto a Entidad (Para guardado)
    public User toEntity(CreateUserDto dto, Role rol, TipoDocumento tipoDoc) {
        if (dto == null || rol == null || tipoDoc == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setNombres(dto.nombres().toUpperCase().trim());
        user.setApellidoPaterno(dto.apellidoPaterno().toUpperCase().trim());
        user.setApellidoMaterno(dto.apellidoMaterno().toUpperCase().trim());
        user.setNumeroDocumento(dto.numeroDocumento());
        user.setTelefono(dto.telefono());
        user.setEmail(dto.email());
        user.setRole(rol);
        user.setTipoDocumento(tipoDoc);

        return user;
    }

    // Actualizar entidad
    public void updateEntityFromDto(CreateUserDto dto, User entity) {
        if (dto == null || entity == null)
            return;

        if (dto.username() != null && !dto.username().isBlank()) {
            entity.setUsername(dto.username());
        }

        if (dto.nombres() != null && !dto.nombres().isBlank()) {
            entity.setNombres(dto.nombres().toUpperCase().trim());
        }

        if (dto.apellidoPaterno() != null && !dto.apellidoPaterno().isBlank()) {
            entity.setApellidoPaterno(dto.apellidoPaterno().toUpperCase().trim());
        }

        if (dto.apellidoMaterno() != null && !dto.apellidoMaterno().isBlank()) {
            entity.setApellidoMaterno(dto.apellidoMaterno().toUpperCase().trim());
        }

        if (dto.numeroDocumento() != null && !dto.numeroDocumento().isBlank()) {
            entity.setNumeroDocumento(dto.numeroDocumento());
        }

        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            entity.setTelefono(dto.telefono());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            entity.setEmail(dto.email());
        }
    }

    // Creacion de MeUserDto
    public MeUserDto toMeUserDto(User user) {
        if (user == null)
            return null;

        return new MeUserDto(
                user.getId(),
                user.getUsername(),
                user.getNombres(),
                user.getApellidoPaterno(),
                user.getApellidoMaterno(),
                user.getTipoDocumento() != null ? user.getTipoDocumento().getName() : null,
                user.getNumeroDocumento(),
                user.getTelefono(),
                user.getEmail(),
                user.getRole().getName());
    }
}
