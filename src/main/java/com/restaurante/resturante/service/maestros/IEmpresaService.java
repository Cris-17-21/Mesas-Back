package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;

public interface IEmpresaService {

    List<EmpresaDto> findAll();

    List<EmpresaDto> findAllActive();

    EmpresaDto findById(String id);

    EmpresaDto create(CreateEmpresaDto dto);

    EmpresaDto update(String id, CreateEmpresaDto dto);

    void delete(String id);
}
