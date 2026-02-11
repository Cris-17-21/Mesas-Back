package com.restaurante.resturante.mapper.maestros;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;
import com.restaurante.resturante.dto.maestro.CreateMesaDto;

@Component
public class MesaMapper {

    public MesaResponseDto toDto(Mesa mesa) {
        if (mesa == null) return null;

        return new MesaResponseDto(
            mesa.getId(),
            mesa.getCodigoMesa(),
            mesa.getCapacidad(),
            mesa.getEstado(),
            mesa.getPiso() != null ? mesa.getPiso().getNombre() : "SIN PISO",
            mesa.getPrincipal() != null ? mesa.getPrincipal().getId() : null
        );
    }

    public Mesa toEntity(CreateMesaDto dto) {
        if (dto == null) return null;
        return Mesa.builder()
            .codigoMesa(dto.codigoMesa())
            .capacidad(dto.capacidad())
            .active(dto.active() != null ? dto.active() : true)
            .estado("LIBRE")
            .build();
    }

    public List<MesaResponseDto> toDTOList(List<Mesa> mesas) {
        if (mesas == null) return List.of();
        return mesas.stream().map(this::toDto).collect(Collectors.toList());
    }
}