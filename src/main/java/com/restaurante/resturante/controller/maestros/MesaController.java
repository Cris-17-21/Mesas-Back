package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.CreateMesaDto;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;
import com.restaurante.resturante.dto.maestro.UnionMesaRequest;
import com.restaurante.resturante.service.maestros.IMesaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final IMesaService mesaService;

    @PreAuthorize("hasAuthority('READ_MESA')")
    @GetMapping("/piso/{pisoId}")
    public ResponseEntity<List<MesaResponseDto>> listarPorPiso(@PathVariable String pisoId) {
        return ResponseEntity.ok(mesaService.findByPiso(pisoId));
    }

    @PreAuthorize("hasAuthority('READ_MESA')")
    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @PreAuthorize("hasAuthority('CREATE_MESA')")
    @PostMapping
    public ResponseEntity<MesaResponseDto> crear(@RequestBody CreateMesaDto dto) {
        return new ResponseEntity<>(mesaService.create(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('UPDATE_MESA')")
    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDto> actualizar(@PathVariable String id, @RequestBody CreateMesaDto dto) {
        return ResponseEntity.ok(mesaService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('UPDATE_MESA')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<MesaResponseDto> cambiarEstado(
            @PathVariable String id, 
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, nuevoEstado));
    }

    @PreAuthorize("hasAuthority('UPDATE_MESA')")
    @PostMapping("/unir")
    public ResponseEntity<Void> unirMesas(@RequestBody UnionMesaRequest request) {
        mesaService.unirMesas(request.idPrincipal(), request.idsSecundarios());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('UPDATE_MESA')")
    @PostMapping("/separar/{idPrincipal}")
    public ResponseEntity<Void> separarMesas(@PathVariable String idPrincipal) {
        mesaService.separarMesas(idPrincipal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('DELETE_MESA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

