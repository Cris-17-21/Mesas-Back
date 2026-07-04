package com.restaurante.resturante.service.maestros;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;

public interface IEmpresaService {

    List<EmpresaDto> findAll();

    List<EmpresaDto> findAllActive();

    EmpresaDto findById(String id);

    EmpresaDto create(CreateEmpresaDto dto);

    EmpresaDto update(String id, CreateEmpresaDto dto);

    void delete(String id);

    EmpresaDto uploadLogo(String id, MultipartFile file);

    EmpresaDto uploadCertificado(String id, MultipartFile file);

    EmpresaDto toggleStatus(String id);
}
