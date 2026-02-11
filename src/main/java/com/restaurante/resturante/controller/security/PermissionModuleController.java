package com.restaurante.resturante.controller.security;

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

import com.restaurante.resturante.dto.security.CreatePermissionModuleDto;
import com.restaurante.resturante.dto.security.PermissionModuleDto;
import com.restaurante.resturante.service.security.IPermissionModuleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permission-modules")
@RequiredArgsConstructor
public class PermissionModuleController {

    private final IPermissionModuleService moduleService;

    @PreAuthorize("hasAuthority('READ_MODULE')")
    @GetMapping
    public ResponseEntity<List<PermissionModuleDto>> getAll() {
        List<PermissionModuleDto> list = moduleService.findAll();
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAuthority('READ_MODULE')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionModuleDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(moduleService.findById(id));
    }

    @PreAuthorize("hasAuthority('READ_MODULE')")
    @GetMapping("/without-children")
    public ResponseEntity<List<PermissionModuleDto>> getModuleWithoutChildren() {
        List<PermissionModuleDto> list = moduleService.findModulesWithoutChildren();
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAuthority('CREATE_MODULE')")
    @PostMapping
    public ResponseEntity<PermissionModuleDto> create(@RequestBody CreatePermissionModuleDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        PermissionModuleDto created = moduleService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('UPDATE_MODULE')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionModuleDto> update(@PathVariable String id, @RequestBody CreatePermissionModuleDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        PermissionModuleDto updated = moduleService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('DELETE_MODULE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
