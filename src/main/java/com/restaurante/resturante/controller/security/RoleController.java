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

import com.restaurante.resturante.dto.security.CreateRoleDto;
import com.restaurante.resturante.dto.security.RoleDto;
import com.restaurante.resturante.service.security.IRoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @PreAuthorize("hasAuthority('READ_ROLE')")
    @GetMapping
    public ResponseEntity<List<RoleDto>> getAll() {
        List<RoleDto> roles = roleService.findAll();
        if (roles.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(roles);
    }

    @PreAuthorize("hasAuthority('READ_ROLE')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PreAuthorize("hasAuthority('CREATE_ROLE')")
    @PostMapping
    public ResponseEntity<RoleDto> create(@RequestBody CreateRoleDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        RoleDto created = roleService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('UPDATE_ROLE')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> update(@PathVariable String id, @RequestBody CreateRoleDto dto) {
        if (dto == null || dto.name() == null || dto.name().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        RoleDto updated = roleService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('DELETE_ROLE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
