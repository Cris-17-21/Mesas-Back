package com.restaurante.resturante.service.security.jpa;

import java.time.LocalDate;
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
import com.restaurante.resturante.dto.security.MeUserDto;
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
public class UserService implements IUserService{

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
        // 1. Cargamos una copia "fresca" del usuario una sola vez.
        User user = userRepository.findByUsernameWithDetails(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        // 2. Construimos la navegación PASANDO EL OBJETO USER DIRECTAMENTE.
        //    ¡Mucho más eficiente! No hay segunda llamada a la BD.
        List<MenuModuleDto> navigation = menuService.buildUserMenu(user);

        // 4. Construimos el DTO del usuario.
        MeUserDto userDto = new MeUserDto(
                user.getId(),
                user.getUsername(),
                user.getNombres(),
                user.getApellidoPaterno(),
                user.getApellidoMaterno(),
                user.getTipoDocumento() != null ? user.getTipoDocumento().getName() : null,
                user.getNumeroDocumento(),
                user.getTelefono(),
                user.getEmail(),
                user.getRole().getName()
        );

        // 5. Ensamblamos y devolvemos la respuesta final.
        return new MeResponseDto(navigation, userDto);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(String obfuscatedId) {
        String id = obfuscatedId;

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return userDtoMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto create(CreateUserDto dto) {

        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalStateException("Ya existe un usuario con nombre '" + dto.username() + "'");
        }

        String roleId = dto.role();
        Role rol = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + dto.role()));


        TipoDocumento tipoDoc = tipoDocumentoRepository.findByName(dto.tipoDocumento())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de documento no encontrado: " + dto.tipoDocumento()));

        User user = userDtoMapper.toEntity(dto, rol, tipoDoc);
        user.setPassword(passwordEncoder.encode(dto.password()));
        User savedUser = userRepository.save(user);

        // 3. ASIGNACIÓN DE ACCESO (Lógica 1 a 1)
        if (dto.empresaId() != null && dto.sucursalId() != null) {
            var empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
            var sucursal = sucursalRepository.findById(dto.sucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

            UserAccess access = UserAccess.builder()
                .user(savedUser)
                .empresa(empresa)
                .sucursal(sucursal)
                .active(true)
                .build();
            
            userAccessRepository.save(access);
        }

        return userDtoMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(String obfuscatedId, CreateUserDto dto) {

        User existing = getUserOrThrow(obfuscatedId);

        if (!existing.getUsername().equals(dto.username()) && userRepository.existsByUsername(dto.username())) {
            throw new IllegalStateException("Ya existe otro usuario con nombre '" + dto.username() + "'");
        }

        String roleId = dto.role();
        Role rol = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + dto.role()));

        existing.setUsername(dto.username());
        existing.setNombres(dto.nombres());
        existing.setApellidoPaterno(dto.apellidoPaterno());
        existing.setApellidoMaterno(dto.apellidoMaterno());
        existing.setTelefono(dto.telefono());
        existing.setEmail(dto.email());
        existing.setRole(rol);


        // Guardar y devolver DTO
        User updated = userRepository.save(existing);
        return userDtoMapper.toUserDto(updated);
    }

    @Override
    @Transactional
    public void delete(String obfuscatedId) {
        User existing = getUserOrThrow(obfuscatedId);
        userRepository.delete(existing);
    }

    private User getUserOrThrow(String obfuscatedId) {
        String id = obfuscatedId;
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User no encontrado"));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUserByEmpresaIdAndSucursalId(String empresaId, String sucursalId) {
        List<User> users = userAccessRepository.findUsersByEmpresaIdAndSucursalId(empresaId, sucursalId);

        if (users.isEmpty()) {
            throw new EntityNotFoundException("No se encontró ningún usuario para la empresa '" + empresaId + "' y sucursal '" + sucursalId + "'");
        }

        return users.stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUserByEmpresaId(String empresaId) {
        List<User> users = userAccessRepository.findUsersByEmpresaId(empresaId);
        return users.stream()
                .map(userDtoMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        return userDtoMapper.toUserDto(user);
    }
}
