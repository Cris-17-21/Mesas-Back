package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.CreateMesaDto;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;

public interface IMesaService {

    List<MesaResponseDto> findByPiso(String pisoId);

    MesaResponseDto create(CreateMesaDto dto);

    MesaResponseDto update(String id, CreateMesaDto dto);
    
    MesaResponseDto obtenerPorId(String id);

    void eliminar(String id);
    
    // Gestión de Estados
    MesaResponseDto cambiarEstado(String id, String nuevoEstado);
    
    // Lógica de Unión
    void unirMesas(String idPrincipal, List<String> idsSecundarios);

    void separarMesas(String idPrincipal); // Para deshacer la unión
}
