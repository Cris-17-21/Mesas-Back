package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.ClienteDto;
import com.restaurante.resturante.dto.maestro.CreateClienteDto;

public interface IClienteService {

    List<ClienteDto> listarPorEmpresa(String empresaId);
    ClienteDto buscarPorDocumento(String numeroDocumento);
    ClienteDto obtenerPorId(String id);
    ClienteDto crear(CreateClienteDto dto);
    ClienteDto actualizar(String id, CreateClienteDto dto);
    void eliminar(String id);
}
