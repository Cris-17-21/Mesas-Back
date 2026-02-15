package com.restaurante.resturante.service.maestros;

import java.util.List;

import com.restaurante.resturante.dto.maestro.CreateMedioPagoDto;
import com.restaurante.resturante.dto.maestro.MedioPagoDto;

public interface IMedioPagoService {
    // Ahora pide el ID explícitamente
    List<MedioPagoDto> listar(String empresaId);

    MedioPagoDto crear(CreateMedioPagoDto dto);

    MedioPagoDto actualizar(String id, CreateMedioPagoDto dto);

    // Para borrar, necesitamos saber de qué empresa es para no borrar el de otro
    void eliminar(String id, String empresaId);

    // Para ver detalle, igual
    MedioPagoDto obtenerPorId(String id, String empresaId);
}
