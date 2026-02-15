package com.restaurante.resturante.service.venta;

import java.util.Optional;

import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;

public interface ICajaTurnoService {

    Optional<CajaTurnoDto> obtenerCajaActiva(String sucursalId, String userId);
    CajaTurnoDto abrirCaja(AbrirCajaDto dto);
    CajaResumentDto obtenerResumenArqueo(String cajaId);
    void cerrarCaja(CerrarCajaDto dto);
    
}
