package com.restaurante.resturante.maestros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.dto.maestro.MasterRegistroDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.service.maestros.IEmpresaService;
import com.restaurante.resturante.service.maestros.ISucursalService;
import com.restaurante.resturante.service.maestros.jpa.UserAcessService;
import com.restaurante.resturante.service.security.IUserService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserAccessTest {

    @Mock
    private IEmpresaService empresaService;
    @Mock
    private ISucursalService sucursalService;
    @Mock
    private IUserService userService;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserAcessService registrationService;

    @Test
    @DisplayName("Debería registrar todo el sistema (Empresa, Sucursal y Admin) exitosamente")
    void registerFullSystem_Success() {
        // GIVEN (Preparación de datos)
        var empresaDtoReq = new CreateEmpresaDto("20123456789", "Restaurante Test", "Calle 1", "999", "e@e.com", null,
                null);
        var sucursalDtoReq = new CreateSucursalDto("Sede Central", "Calle 1", "999", null);
        var userDtoReq = new CreateUserDto("admin", "123", "Juan", "Perez", "Soto", "DNI", "12345678", "999", "j@j.com",
                null, null, null);

        var request = new MasterRegistroDto(empresaDtoReq, sucursalDtoReq, userDtoReq);

        // Mocks de respuestas de los servicios
        var empresaSaved = new EmpresaDto("EMP-ID-123", "20123456789", "Restaurante Test", "Calle 1", "999", "e@e.com",
                null, null, null);
        var sucursalSaved = new SucursalDto("SUC-ID-456", "Sede Central", "Calle 1", "999", "Restaurante Test");
        var adminRole = Role.builder().id("ROLE-ID-UUID").name("ADMIN_RESTAURANTE").build();
        var finalUserDto = new UserDto("USER-ID", "admin", "Juan", "Perez", "Soto", "DNI", "12345678", "999", "j@j.com",
                "ADMIN_RESTAURANTE");

        when(empresaService.create(any())).thenReturn(empresaSaved);
        when(sucursalService.create(any())).thenReturn(sucursalSaved);
        when(roleRepository.findByName("ADMIN_RESTAURANTE")).thenReturn(Optional.of(adminRole));
        when(userService.create(any())).thenReturn(finalUserDto);

        // WHEN (Ejecución)
        UserDto result = registrationService.registrarUsuarioAdmin(request);

        // THEN (Verificaciones)
        assertNotNull(result);
        assertEquals("USER-ID", result.id());

        // Verificar que se pasaron los IDs correctos al crear el usuario
        verify(userService).create(argThat(dto -> dto.empresaId().equals("EMP-ID-123") &&
                dto.sucursalId().equals("SUC-ID-456") &&
                dto.role().equals("ROLE-ID-UUID")));

        verify(empresaService, times(1)).create(any());
        verify(sucursalService, times(1)).create(any());
    }

    @Test
    @DisplayName("Debería fallar si el rol ADMIN_RESTAURANTE no existe")
    void registerFullSystem_RoleNotFound_ThrowsException() {
        // GIVEN
        // Enviamos DTOs vacíos pero NO nulos para evitar el NullPointerException
        // inicial
        var empresaReq = new CreateEmpresaDto("20123456789", "Test", "Dir", "999", "e@e.com", null, null);
        var sucursalReq = new CreateSucursalDto("Suc", "Dir", "999", null);
        var userReq = new CreateUserDto("u", "p", "n", "ap", "am", "DNI", "12345678", "9", "e", null, null, null);

        var request = new MasterRegistroDto(empresaReq, sucursalReq, userReq);

        // Mockeamos que la empresa y sucursal se crean bien
        when(empresaService.create(any()))
                .thenReturn(new EmpresaDto("ID", "20123456789", "Test", "Dir", "9", "e", null, null, null));
        when(sucursalService.create(any())).thenReturn(new SucursalDto("ID", "Suc", "Dir", "9", "Test"));

        // Aquí es donde forzamos el error que realmente queremos probar
        when(roleRepository.findByName("ADMIN_RESTAURANTE")).thenReturn(Optional.empty());

        // WHEN & THEN
        // Ahora sí, debería llegar a buscar el rol y lanzar la excepción correcta
        assertThrows(EntityNotFoundException.class, () -> registrationService.registrarUsuarioAdmin(request));
        verify(userService, never()).create(any());
    }
}
