package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.CreatePisoDto;
import com.restaurante.resturante.dto.maestro.PisoDto;

public interface IPisoService {

    List<PisoDto> findAllBySucursal(String sucursalId);

    PisoDto findById(String id);

    PisoDto create(CreatePisoDto dto);

    PisoDto update(String id, CreatePisoDto dto);
    
    void delete(String id);
}
