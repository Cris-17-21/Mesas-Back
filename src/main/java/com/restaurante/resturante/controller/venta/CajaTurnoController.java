package com.restaurante.resturante.controller.venta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;
import com.restaurante.resturante.service.venta.ICajaTurnoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas/caja")
@RequiredArgsConstructor
public class CajaTurnoController {

    private final ICajaTurnoService cajaService;

    // Para saber si el cajero debe ver el formulario de apertura o el panel
    @GetMapping("/activa/{sucursalId}/{usuarioId}")
    public ResponseEntity<CajaTurnoDto> obtenerCajaActiva(
            @PathVariable String sucursalId,
            @PathVariable String usuarioId) {
        return cajaService.obtenerCajaActiva(sucursalId, usuarioId)
            .map(ResponseEntity::ok) // Si existe, devuelve 200 + DTO
            .orElse(ResponseEntity.noContent().build()); // Si no existe, devuelve 204 (o .ok(null))
    }

    @PostMapping("/abrir")
    public ResponseEntity<CajaTurnoDto> abrir(@RequestBody AbrirCajaDto dto) {
        // PERMISO: ABRIR_CAJA
        return ResponseEntity.ok(cajaService.abrirCaja(dto));
    }

    @GetMapping("/arqueo/{cajaId}")
    public ResponseEntity<CajaResumentDto> verArqueo(@PathVariable String cajaId) {
        // PERMISO: CONSULTAR_ARQUEO
        return ResponseEntity.ok(cajaService.obtenerResumenArqueo(cajaId));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrar(@RequestBody CerrarCajaDto dto) {
        // PERMISO: CERRAR_CAJA
        cajaService.cerrarCaja(dto);
        return ResponseEntity.noContent().build();
    }
}
