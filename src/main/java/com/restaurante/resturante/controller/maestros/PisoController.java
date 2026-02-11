package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.CreatePisoDto;
import com.restaurante.resturante.dto.maestro.PisoDto;
import com.restaurante.resturante.service.maestros.IPisoService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/pisos")
@RequiredArgsConstructor
public class PisoController {

    private final IPisoService pisoService;

    @PreAuthorize("hasAuthority('READ_PISO')")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<PisoDto>> getBySucursal(@PathVariable String sucursalId) {
        return ResponseEntity.ok(pisoService.findAllBySucursal(sucursalId));
    }

    @PreAuthorize("hasAuthority('READ_PISO')")
    @GetMapping("/{id}")
    public ResponseEntity<PisoDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(pisoService.findById(id));
    }
    
    @PreAuthorize("hasAuthority('CREATE_PISO')")
    @PostMapping
    public ResponseEntity<PisoDto> create(@RequestBody CreatePisoDto dto) {
        return ResponseEntity.ok(pisoService.create(dto));
    }

    @PreAuthorize("hasAuthority('UPDATE_PISO')")
    @PutMapping("/{id}")
    public ResponseEntity<PisoDto> update(@PathVariable String id, @RequestBody CreatePisoDto dto) {
        return ResponseEntity.ok(pisoService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('DELETE_PISO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        pisoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
