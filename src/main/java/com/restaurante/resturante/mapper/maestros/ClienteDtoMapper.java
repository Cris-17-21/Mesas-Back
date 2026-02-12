package com.restaurante.resturante.mapper.maestros;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.dto.maestro.ClienteDto;
import com.restaurante.resturante.dto.maestro.CreateClienteDto;

@Component
public class ClienteDtoMapper {

    public ClienteDto toDto(Cliente entity) {
        if (entity == null) return null;
        return new ClienteDto(
            entity.getId(),
            entity.getTipoDocumento() != null ? entity.getTipoDocumento().getName() : "SIN TIPO",
            entity.getNumeroDocumento(),
            entity.getNombreRazonSocial(),
            entity.getDireccion(),
            entity.getCorreo(),
            entity.getTelefono(),
            entity.getActive()
        );
    }

    public Cliente toEntity(CreateClienteDto dto) {
        if (dto == null) return null;
        return Cliente.builder()
            .numeroDocumento(dto.numeroDocumento())
            .nombreRazonSocial(dto.nombreRazonSocial().toUpperCase()) // Persistencia en MAYÚSCULAS
            .direccion(dto.direccion())
            .correo(dto.correo())
            .telefono(dto.telefono())
            .active(true)
            .build();
    }
}
