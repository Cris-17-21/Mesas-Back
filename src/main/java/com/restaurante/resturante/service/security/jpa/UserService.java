package com.restaurante.resturante.service.security.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Sucursal;

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
        if (!user.getActive()) {
            throw new IllegalStateException("El usuario se encuentra inactivo.");
        }
        List<MenuModuleDto> navigation = menuService.buildUserMenu(user);
        return new MeResponseDto(navigation, userDtoMapper.toMeUserDto(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(String id) {
        User target = getUserOrThrow(id);
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            validateTargetUserBelongsToCompany(target, empresaId);
        }
        return userDtoMapper.toUserDto(target);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId != null) {
                return userAccessRepository.findByEmpresaId(empresaId).stream()
                        .map(UserAccess::getUser)
                        .distinct()
                        .map(userDtoMapper::toUserDto)
                        .toList();
            }
        }
        return userRepository.findAll().stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllActiveUsers() {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId != null) {
                return userAccessRepository.findActiveUsersByEmpresaId(empresaId).stream()
                        .filter(User::getActive)
                        .map(userDtoMapper::toUserDto)
                        .toList();
            }
        }
        return userRepository.findAllByActiveTrue().stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto create(CreateUserDto dto) {
        Role rol = getRoleOrThrow(dto.role());
        TipoDocumento tipoDoc = getTipoDocOrThrow(dto.tipoDocumento());

        String targetEmpresaId = dto.empresaId();
        String targetSucursalId = dto.sucursalId();

        if (isAuthenticatedUserRestaurantAdmin()) {
            if (rol.getName().equals("ROLE_SUPER_ADMIN")) {
                throw new AccessDeniedException("No tienes permiso para asignar este rol.");
            }
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (adminEmpresaId == null) {
                throw new AccessDeniedException("No se encontró tu empresa.");
            }
            targetEmpresaId = adminEmpresaId;

            if (targetSucursalId != null) {
                Sucursal sucursal = sucursalRepository.findById(targetSucursalId)
                        .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
                if (!sucursal.getEmpresa().getId().equals(adminEmpresaId)) {
                    throw new AccessDeniedException("La sucursal indicada no pertenece a tu empresa.");
                }
            }
        }

        // Buscamos si el usuario existe por DNI o Username (sin importar si está activo
        // o no)
        Optional<User> userByUsername = userRepository.findByUsername(dto.username());
        Optional<User> userByDoc = userRepository.findByNumeroDocumento(dto.numeroDocumento());

        User existing = userByUsername.orElse(userByDoc.orElse(null));

        if (existing != null) {
            // --- CASO: EL USUARIO YA EXISTE (ACTIVO O INACTIVO) ---
            if (isAuthenticatedUserRestaurantAdmin()) {
                validateTargetUserBelongsToCompany(existing, targetEmpresaId);
            }
            // Actualizamos sus datos básicos por si cambiaron
            userDtoMapper.updateEntityFromDto(dto, existing);
            existing.setRole(rol);
            existing.setTipoDocumento(tipoDoc);
            existing.setActive(true); // Nos aseguramos de que esté activo

            User savedUser = userRepository.save(existing);

            // Asignamos el acceso (Aquí se aplica la regla de MAYÚSCULAS)
            assignInitialAccess(savedUser, targetEmpresaId, targetSucursalId);

            return userDtoMapper.toUserDto(savedUser);
        }

        // --- CASO: USUARIO COMPLETAMENTE NUEVO ---
        User user = userDtoMapper.toEntity(dto, rol, tipoDoc);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setActive(true);
        User savedUser = userRepository.save(user);

        assignInitialAccess(savedUser, targetEmpresaId, targetSucursalId);

        return userDtoMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(String id, CreateUserDto dto) {
        User existing = getUserOrThrow(id);

        Role rol = getRoleOrThrow(dto.role());
        String targetEmpresaId = dto.empresaId();
        String targetSucursalId = dto.sucursalId();

        if (isAuthenticatedUserRestaurantAdmin()) {
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            validateTargetUserBelongsToCompany(existing, adminEmpresaId);

            if (existing.getRole() != null && existing.getRole().getName().equals("ROLE_SUPER_ADMIN")) {
                throw new AccessDeniedException("No tienes permiso para modificar a un super administrador.");
            }
            if (rol.getName().equals("ROLE_SUPER_ADMIN")) {
                throw new AccessDeniedException("No tienes permiso para asignar el rol de super administrador.");
            }
            targetEmpresaId = adminEmpresaId;

            if (targetSucursalId != null) {
                Sucursal sucursal = sucursalRepository.findById(targetSucursalId)
                        .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
                if (!sucursal.getEmpresa().getId().equals(adminEmpresaId)) {
                    throw new AccessDeniedException("La sucursal indicada no pertenece a tu empresa.");
                }
            }
        }

        if (!existing.getUsername().equalsIgnoreCase(dto.username())) {
            validateUsernameUnique(dto.username());
        }
        if (!existing.getNumeroDocumento().equalsIgnoreCase(dto.numeroDocumento())) {
            validateDocumentoUnique(dto.numeroDocumento());
        }

        // Usamos el método que ya tienes en el mapper para actualizar campos básicos
        userDtoMapper.updateEntityFromDto(dto, existing);
        existing.setRole(rol);

        if (dto.password() != null && !dto.password().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.password()));
        }

        User savedUser = userRepository.save(existing);

        // Actualizamos el acceso si se proporcionan los datos
        if (targetEmpresaId != null || targetSucursalId != null) {
            updateUserAccess(savedUser, targetEmpresaId, targetSucursalId);
        }

        return userDtoMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public void delete(String id) {
        User user = getUserOrThrow(id);
        if (isAuthenticatedUserRestaurantAdmin()) {
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            validateTargetUserBelongsToCompany(user, adminEmpresaId);
            if (user.getRole() != null && user.getRole().getName().equals("ROLE_SUPER_ADMIN")) {
                throw new AccessDeniedException("No tienes permiso para eliminar a un super administrador.");
            }
        }
        user.setActive(false);
        userRepository.save(user);
    }

    // --- MÉTODOS DE BÚSQUEDA ESPECIALIZADA ---

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserByUsername(String username) {
        User target = getUserByUsernameOrThrow(username);
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            validateTargetUserBelongsToCompany(target, empresaId);
        }
        return userDtoMapper.toUserDto(target);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUserByEmpresaId(String empresaId) {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (adminEmpresaId == null || !adminEmpresaId.equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para consultar los usuarios de esta empresa.");
            }
        }
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
        if (isAuthenticatedUserRestaurantAdmin()) {
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (adminEmpresaId == null || !adminEmpresaId.equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para consultar los usuarios de esta empresa.");
            }
        }
        List<User> users = userAccessRepository.findUsersByEmpresaIdAndSucursalId(empresaId, sucursalId);
        if (users.isEmpty())
            throw new EntityNotFoundException("No se encontraron usuarios para la sede indicada.");
        return users.stream().map(userDtoMapper::toUserDto).toList();
    }

    // -------- MÉTODOS PRIVADOS DE SOPORTE --------

    private void assignInitialAccess(User user, String empresaId, String sucursalId) {
        updateUserAccess(user, empresaId, sucursalId);
    }

    private void updateUserAccess(User user, String empresaId, String sucursalId) {
        if (empresaId != null && sucursalId != null) {
            // Desactivamos accesos previos para evitar duplicidad o conflictos
            List<UserAccess> currentAccesses = userAccessRepository.findByUserIdAndActiveTrue(user.getId());
            for (UserAccess acc : currentAccesses) {
                if (!acc.getSucursal().getId().equals(sucursalId)) {
                    acc.setActive(false);
                    userAccessRepository.save(acc);
                }
            }

            // Buscamos si ya existe este acceso específico (inactivo o activo)
            List<UserAccess> accesosExistentes = userAccessRepository
                    .findByUserIdAndSucursalId(user.getId(), sucursalId);

            UserAccess access;

            if (!accesosExistentes.isEmpty()) {
                access = accesosExistentes.get(0);
            } else {
                access = new UserAccess();
                access.setUser(user);
                access.setEmpresa(empresaRepository.getReferenceById(empresaId));
                access.setSucursal(sucursalRepository.getReferenceById(sucursalId));
            }

            access.setActive(true);
            userAccessRepository.save(access);
        }
    }

    private void validateUsernameUnique(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("El nombre de usuario '" + username + "' ya está en uso.");
        }
    }

    private void validateDocumentoUnique(String numeroDocumento) {
        if (userRepository.existsByNumeroDocumentoAndActiveTrue(numeroDocumento)) {
            throw new IllegalStateException("El número de documento '" + numeroDocumento + "' ya está registrado.");
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

    private boolean isAuthenticatedUserRestaurantAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_RESTAURANTE"));
    }

    private String getAuthenticatedUserEmpresaIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        List<UserAccess> accesses = userAccessRepository.findByUserUsername(username);
        return accesses.stream()
                .filter(UserAccess::getActive)
                .map(acc -> acc.getEmpresa().getId())
                .findFirst()
                .orElse(null);
    }

    private void validateTargetUserBelongsToCompany(User targetUser, String empresaId) {
        if (targetUser == null || empresaId == null) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }
        List<UserAccess> targetAccesses = userAccessRepository.findByUserIdAndActiveTrue(targetUser.getId());
        boolean sameCompany = targetAccesses.stream()
                .anyMatch(acc -> acc.getEmpresa().getId().equals(empresaId));
        if (!sameCompany) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }
    }
}