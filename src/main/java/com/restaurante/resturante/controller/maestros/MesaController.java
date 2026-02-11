package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.MesaResponseDto;
import com.restaurante.resturante.service.maestros.jpa.MesaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public ResponseEntity<List<MesaResponseDto>> listarTodas() {
        return ResponseEntity.ok(mesaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @GetMapping("/piso/{pisoId}")
    public ResponseEntity<List<MesaResponseDto>> listarPorPiso(@PathVariable String pisoId) {
        return ResponseEntity.ok(mesaService.listarActivasPorPiso(pisoId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MesaResponseDto> cambiarEstado(
            @PathVariable String id, 
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, nuevoEstado));
    }

    @PostMapping("/unir")
    public ResponseEntity<Void> unirMesas(
            @RequestParam String idPrincipal, 
            @RequestBody List<String> idsSecundarios) {
        mesaService.unirMesas(idPrincipal, idsSecundarios);
        return ResponseEntity.noContent().build();
    }
}
