package com.restaurante.resturante.mapper.maestros;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.dto.maestro.CreateMedioPagoDto;
import com.restaurante.resturante.dto.maestro.MedioPagoDto;

@Component
public class MedioPagoMapper {

    // 1. De Entidad -> Record (Para enviar al Frontend)
    public MedioPagoDto toDto(MedioPago entity) {
        if (entity == null)
            return null;

        return new MedioPagoDto(
                entity.getId(),
                entity.getNombre(),
                entity.isEsEfectivo(),
                entity.getEmpresa().getRazonSocial());
    }

    // 2. De Record -> Entidad (Para crear en BD)
    public MedioPago toEntity(CreateMedioPagoDto dto) {
        if (dto == null)
            return null;

        MedioPago entity = new MedioPago();
        entity.setNombre(dto.nombre().toUpperCase().trim());

        // El ID y EmpresaID se setean en el servicio
        return entity;
    }

    // 3. Actualizar Entidad existente (Para el PUT)
    public void updateEntityFromDto(CreateMedioPagoDto dto, MedioPago entity) {
        if (dto == null || entity == null)
            return;

        entity.setNombre(dto.nombre().toUpperCase().trim());
    }
}
