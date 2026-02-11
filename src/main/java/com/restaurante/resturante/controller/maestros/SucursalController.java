package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.service.maestros.ISucursalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final ISucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalDto>> getAll() {
        return ResponseEntity.ok(sucursalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(sucursalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SucursalDto> create(@RequestBody CreateSucursalDto dto) {
        SucursalDto created = sucursalService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalDto> update(@PathVariable String id, @RequestBody CreateSucursalDto dto) {
        return ResponseEntity.ok(sucursalService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        sucursalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<SucursalDto>> getByEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(sucursalService.findByEmpresaId(empresaId));
    }
}
