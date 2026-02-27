package com.restaurante.resturante.controller.maestros;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.MasterRegistroDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.service.maestros.IUserAccessService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAccessController {

    private final IUserAccessService registrationService;

    @PostMapping("/register-enterprise")
    public ResponseEntity<UserDto> registerSystem(@Valid @RequestBody MasterRegistroDto request) {
        // El servicio orquestador devuelve un UserDto, el controller lo entrega al
        // Front
        UserDto createdUser = registrationService.registrarUsuarioAdmin(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}
