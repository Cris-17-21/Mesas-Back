package com.restaurante.resturante.controller.venta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;
import com.restaurante.resturante.service.venta.ICajaTurnoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/caja-turnos")
@RequiredArgsConstructor
public class CajaTurnoController {

    private final ICajaTurnoService service;

    /**
     * Endpoint para abrir un nuevo turno de caja.
     * POST /api/v1/caja-turnos/abrir
     */
    @PostMapping("/abrir")
    public ResponseEntity<CajaTurnoDto> abrirTurno(@RequestBody AbrirCajaDto dto) {
        return new ResponseEntity<>(service.abrirTurno(dto), HttpStatus.CREATED);
    }

    /**
     * Obtiene el turno activo para un usuario en una sucursal específica.
     * Útil para que el Front verifique el estado al cargar la página de ventas.
     * GET /api/v1/caja-turnos/activo?usuarioId=...&sucursalId=...
     */
    @GetMapping("/activo")
    public ResponseEntity<CajaTurnoDto> getTurnoActivo(
            @RequestParam String usuarioId, 
            @RequestParam String sucursalId) {
        CajaTurnoDto dto = service.getTurnoActivo(usuarioId, sucursalId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el resumen financiero (ventas, efectivo, tarjetas) del turno actual.
     * GET /api/v1/caja-turnos/{id}/resumen
     */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<CajaResumentDto> getResumen(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerResumenActual(id));
    }

    /**
     * Endpoint para cerrar el turno de caja.
     * POST /api/v1/caja-turnos/cerrar
     */
    @PostMapping("/cerrar")
    public ResponseEntity<CajaTurnoDto> cerrarTurno(@RequestBody CerrarCajaDto dto) {
        return ResponseEntity.ok(service.cerrarTurno(dto));
    }
}
