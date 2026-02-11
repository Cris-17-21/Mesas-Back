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

import com.restaurante.resturante.dto.security.CreatePermissionDto;
import com.restaurante.resturante.dto.security.PermissionDto;
import com.restaurante.resturante.service.security.IPermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @PreAuthorize("hasAuthority('READ_PERMISSION')")
    @GetMapping
    public ResponseEntity<List<PermissionDto>> getAll() {
        List<PermissionDto> list = permissionService.findAll();
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAuthority('READ_PERMISSION')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PreAuthorize("hasAuthority('CREATE_PERMISSION')")
    @PostMapping
    public ResponseEntity<PermissionDto> create(@RequestBody CreatePermissionDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank() || dto.description() == null || dto.description().isBlank() || dto.moduleId() == null || dto.moduleId().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        PermissionDto created = permissionService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('UPDATE_PERMISSION')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionDto> update(@PathVariable String id, @RequestBody CreatePermissionDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank() || dto.description() == null || dto.description().isBlank() || dto.moduleId() == null || dto.moduleId().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        PermissionDto updated = permissionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('DELETE_PERMISSION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
