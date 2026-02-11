package com.restaurante.resturante.mapper.maestros;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Piso;
import com.restaurante.resturante.dto.maestro.CreatePisoDto;
import com.restaurante.resturante.dto.maestro.PisoDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PisoDtoMapper {

    private final MesaMapper mesaMapper;

    public PisoDto toDto(Piso entity) {
        if (entity == null) return null;
        return new PisoDto(
            entity.getId(),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.getSucursal() != null ? entity.getSucursal().getNombre() : null,
            entity.getMesas() != null 
                ? entity.getMesas().stream().map(mesaMapper::toDto).collect(Collectors.toList())
                : java.util.List.of()
        );
    }

    public Piso toEntity(CreatePisoDto dto) {
        if (dto == null) return null;
        return Piso.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .active(true)
            .build();
    }
}
