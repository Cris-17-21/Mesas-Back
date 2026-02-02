package com.restaurante.resturante.service.maestros;

import java.util.List;
import java.util.UUID;

import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;

public interface ISucursalService {

    List<SucursalDto> findAll();

    SucursalDto findById(String id);

    SucursalDto create(CreateSucursalDto dto);

    SucursalDto update(String id, CreateSucursalDto dto);

    void delete(String id);

    List<SucursalDto> findByEmpresaId(String empresaId);
}
