package com.restaurante.resturante.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.mapper.security.UserDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.security.jpa.MenuService;
import com.restaurante.resturante.service.security.jpa.UserService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserAccessRepository userAccessRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MenuService menuService;
    @Mock
    private UserDtoMapper userDtoMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Debería crear un usuario y asignar acceso inicial exitosamente")
    void create_Success() {
        // GIVEN
        CreateUserDto dto = new CreateUserDto(
                "admin", "password123", "Juan", "Perez", "Soto",
                "DNI", "12345678", "999", "test@test.com",
                "role-id", "emp-1", "suc-1");

        Role role = new Role();
        TipoDocumento td = new TipoDocumento();
        User userEntity = new User();
        userEntity.setUsername("admin");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findById("role-id")).thenReturn(Optional.of(role));
        when(tipoDocumentoRepository.findByName("DNI")).thenReturn(Optional.of(td));
        when(userDtoMapper.toEntity(any(), any(), any())).thenReturn(userEntity);
        when(passwordEncoder.encode(any())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        // Mocks para el acceso inicial
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(new Empresa()));
        when(sucursalRepository.findById("suc-1")).thenReturn(Optional.of(new Sucursal()));

        // WHEN
        userService.create(dto);

        // THEN
        verify(userRepository).save(any(User.class));
        verify(userAccessRepository).save(any(UserAccess.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Debería lanzar excepción si el nombre de usuario ya existe")
    void create_UsernameExists_ThrowsException() {
        // GIVEN
        CreateUserDto dto = new CreateUserDto("admin", "123", "N", "A", "A", "DNI", "1", "1", "e", "r", "e", "s");
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería obtener detalles para 'Me' correctamente")
    void getUserDetailsForMe_Success() {
        // GIVEN
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(menuService.buildUserMenu(user)).thenReturn(List.of());

        // WHEN
        userService.getUserDetailsForMe(username);

        // THEN
        verify(menuService).buildUserMenu(user);
        verify(userDtoMapper).toMeUserDto(user);
    }

    @Test
    @DisplayName("Debería fallar al actualizar si el usuario no existe")
    void update_UserNotFound_ThrowsException() {
        // GIVEN
        when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> userService.update("invalid-id", null));
    }

    @Test
    @DisplayName("Debería listar usuarios por empresa y sucursal")
    void getUserByEmpresaAndSucursal_Success() {
        // GIVEN
        String eId = "emp-1";
        String sId = "suc-1";
        when(userAccessRepository.findUsersByEmpresaIdAndSucursalId(eId, sId))
                .thenReturn(List.of(new User()));

        // WHEN
        List<UserDto> result = userService.getUserByEmpresaIdAndSucursalId(eId, sId);

        // THEN
        assertFalse(result.isEmpty());
        verify(userAccessRepository).findUsersByEmpresaIdAndSucursalId(eId, sId);
    }
}
