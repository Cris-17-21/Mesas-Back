package com.restaurante.resturante.mapper.security;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.enums.Sexo;
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
                user.getSexo() != null ? user.getSexo().name() : null,
                user.getFechaNacimiento() != null ? user.getFechaNacimiento().toString() : null,
                user.getTelefono(),
                user.getDireccion(),
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
        user.setPassword(dto.password());
        user.setNombres(dto.nombres());
        user.setApellidoPaterno(dto.apellidoPaterno());
        user.setApellidoMaterno(dto.apellidoMaterno());
        user.setNumeroDocumento(dto.numeroDocumento());
        user.setTelefono(dto.telefono());
        user.setDireccion(dto.direccion());
        user.setEmail(dto.email());
        user.setRole(rol);
        user.setTipoDocumento(tipoDoc);

        if (dto.sexo() != null) {
            user.setSexo(Sexo.valueOf(dto.sexo().toUpperCase()));
        }

        if (dto.fechaNacimiento() != null) {
            try {
                user.setFechaNacimiento(LocalDate.parse(dto.fechaNacimiento()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha inválido. Use AAAA-MM-DD");
            }
        }

        return user;
    }
}
