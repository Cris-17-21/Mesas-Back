package com.restaurante.resturante.mapper.maestros;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;

@Component
public class MesaMapper {

    public MesaResponseDto toDTO(Mesa mesa) {
        if (mesa == null) return null;

        return new MesaResponseDto(
            mesa.getId(),
            mesa.getCodigoMesa(),
            mesa.getCapacidad(),
            mesa.getEstado(),
            mesa.getActive(), // Mapea tu variable 'active' de la entidad
            mesa.getPiso() != null ? mesa.getPiso().getNombre() : "SIN PISO",
            mesa.getPrincipal() != null ? mesa.getPrincipal().getId() : null
        );
    }

    public List<MesaResponseDto> toDTOList(List<Mesa> mesas) {
        if (mesas == null) return List.of();
        return mesas.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
    }
}
