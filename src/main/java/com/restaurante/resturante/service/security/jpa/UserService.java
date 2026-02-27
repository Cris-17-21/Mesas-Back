package com.restaurante.resturante.service.security.jpa;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.MeResponseDto;
import com.restaurante.resturante.dto.security.MenuModuleDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.mapper.security.UserDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.security.IUserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserAccessRepository userAccessRepository;
    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;
    private final UserDtoMapper userDtoMapper;
    private final RoleRepository roleRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Transactional(readOnly = true)
    @Override
    public MeResponseDto getUserDetailsForMe(String username) {
        User user = getUserByUsernameOrThrow(username);
        List<MenuModuleDto> navigation = menuService.buildUserMenu(user);

        // Delegamos la creación del MeUserDto al mapper (puedes añadir este método al
        // mapper luego)
        return new MeResponseDto(navigation, userDtoMapper.toMeUserDto(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(String id) {
        // Aprovechamos el método privado de soporte para mantener la limpieza
        return userDtoMapper.toUserDto(getUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto create(CreateUserDto dto) {
        validateUsernameUnique(dto.username());

        Role rol = getRoleOrThrow(dto.role());
        TipoDocumento tipoDoc = getTipoDocOrThrow(dto.tipoDocumento());

        User user = userDtoMapper.toEntity(dto, rol, tipoDoc);
        user.setPassword(passwordEncoder.encode(dto.password()));
        User savedUser = userRepository.save(user);

        // Orquestación de acceso inicial
        assignInitialAccess(savedUser, dto.empresaId(), dto.sucursalId());

        return userDtoMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(String id, CreateUserDto dto) {
        User existing = getUserOrThrow(id);

        if (!existing.getUsername().equalsIgnoreCase(dto.username())) {
            validateUsernameUnique(dto.username());
        }

        Role rol = getRoleOrThrow(dto.role());

        // Usamos el método que ya tienes en el mapper para actualizar campos básicos
        userDtoMapper.updateEntityFromDto(dto, existing);
        existing.setRole(rol);

        return userDtoMapper.toUserDto(userRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        userRepository.delete(getUserOrThrow(id));
    }

    // --- MÉTODOS DE BÚSQUEDA ESPECIALIZADA ---

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserByUsername(String username) {
        return userDtoMapper.toUserDto(getUserByUsernameOrThrow(username));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUserByEmpresaId(String empresaId) {
        // Validamos que la empresa exista antes de buscar
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa no encontrada con ID: " + empresaId);
        }

        return userAccessRepository.findActiveUsersByEmpresaId(empresaId).stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUserByEmpresaIdAndSucursalId(String empresaId, String sucursalId) {
        List<User> users = userAccessRepository.findUsersByEmpresaIdAndSucursalId(empresaId, sucursalId);
        if (users.isEmpty())
            throw new EntityNotFoundException("No se encontraron usuarios para la sede indicada.");
        return users.stream().map(userDtoMapper::toUserDto).toList();
    }

    // -------- MÉTODOS PRIVADOS DE SOPORTE --------

    private void assignInitialAccess(User user, String empresaId, String sucursalId) {
        if (empresaId != null && sucursalId != null) {
            var empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
            var sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

            userAccessRepository.save(UserAccess.builder()
                    .user(user).empresa(empresa).sucursal(sucursal).active(true).build());
        }
    }

    private void validateUsernameUnique(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("El nombre de usuario '" + username + "' ya está en uso.");
        }
    }

    private User getUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    private User getUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
    }

    private Role getRoleOrThrow(String roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado"));
    }

    private TipoDocumento getTipoDocOrThrow(String name) {
        return tipoDocumentoRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de documento no encontrado: " + name));
    }
}