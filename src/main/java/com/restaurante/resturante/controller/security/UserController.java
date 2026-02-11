package com.restaurante.resturante.controller.security;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.MeResponseDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.service.security.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        MeResponseDto response = userService.getUserDetailsForMe(username);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/{obfuscatedId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String obfuscatedId) {
        UserDto user = userService.getUserById(obfuscatedId);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();    
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/empresa/{empresaId}/sucursal/{sucursalId}")
    public ResponseEntity<List<UserDto>> getUsersByEmpresaAndSucursal(
            @PathVariable String empresaId,
            @PathVariable String sucursalId) {
        List<UserDto> users = userService.getUserByEmpresaIdAndSucursalId(empresaId, sucursalId);
        return ResponseEntity.ok(users);
    }


    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<UserDto>> getUsersByEmpresa(@PathVariable String empresaId) {
        List<UserDto> users = userService.getUserByEmpresaId(empresaId);
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody CreateUserDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable("id") String obfuscatedId, @RequestBody CreateUserDto dto) {
        UserDto updated = userService.update(obfuscatedId, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String obfuscatedId) {
        userService.delete(obfuscatedId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
