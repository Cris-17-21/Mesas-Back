package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.MesaResponseDto;

public interface IMesaService {

    List<MesaResponseDto> listarTodas();
    
    List<MesaResponseDto> listarActivasPorPiso(String pisoId);
    
    MesaResponseDto obtenerPorId(String id);
    
    // Cambiar estado de la mesa (LIBRE, OCUPADA, SUCIA, RESERVADA)
    MesaResponseDto cambiarEstado(String id, String nuevoEstado);
    
    // Lógica para unir mesas (Mesa A se une a Mesa B)
    void unirMesas(String idPrincipal, List<String> idsSecundarios);
}
