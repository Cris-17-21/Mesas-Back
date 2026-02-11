package com.restaurante.resturante.service.venta;

import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;

public interface ICajaTurnoService {

    CajaTurnoDto abrirTurno(AbrirCajaDto dto);

    CajaResumentDto obtenerResumenActual(String turnoId);

    CajaTurnoDto cerrarTurno(CerrarCajaDto dto);

    CajaTurnoDto getTurnoActivo(String usuarioId, String sucursalId);
    
}
