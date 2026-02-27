package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.service.maestros.IEmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final IEmpresaService empresaService;

    @PreAuthorize("hasAuthority('READ_EMPRESA')")
    @GetMapping
    public ResponseEntity<List<EmpresaDto>> getAll() {
        return ResponseEntity.ok(empresaService.findAll());
    }

    @PreAuthorize("hasAuthority('READ_EMPRESA')")
    @GetMapping("/active")
    public ResponseEntity<List<EmpresaDto>> getAllActive() {
        return ResponseEntity.ok(empresaService.findAllActive());
    }

    @PreAuthorize("hasAuthority('READ_EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(empresaService.findById(id));
    }

    @PreAuthorize("hasAuthority('CREATE_EMPRESA')")
    @PostMapping
    public ResponseEntity<EmpresaDto> create(@Valid @RequestBody CreateEmpresaDto dto) {
        EmpresaDto created = empresaService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('UPDATE_EMPRESA')")
    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDto> update(@PathVariable String id, @Valid @RequestBody CreateEmpresaDto dto) {
        return ResponseEntity.ok(empresaService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('DELETE_EMPRESA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        empresaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
