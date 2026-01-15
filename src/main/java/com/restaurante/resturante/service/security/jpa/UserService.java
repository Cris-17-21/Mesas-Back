package com.restaurante.resturante.service.security.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.MeResponseDto;
import com.restaurante.resturante.dto.security.MeUserDto;
import com.restaurante.resturante.dto.security.MenuModuleDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.mapper.security.UserDtoMapper;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.security.IUserService;
import com.restaurante.resturante.service.security.IdEncryptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final MenuService menuService;
    private final IdEncryptionService idEncryptionService;
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
                user.getSexo() != null ? user.getSexo().name() : null,
                user.getFechaNacimiento() != null ? user.getFechaNacimiento().toString() : null,
                user.getTelefono(),
                user.getDireccion(),
                user.getEmail(),
                user.getRole().getName()
        );

        // 5. Ensamblamos y devolvemos la respuesta final.
        return new MeResponseDto(navigation, userDto);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(String obfuscatedId) {
        long id = idEncryptionService.decrypt(obfuscatedId);
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido o manipulado");
        }

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

        long roleId = idEncryptionService.decrypt(dto.role());
        Role rol = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + dto.role()));


        TipoDocumento tipoDoc = tipoDocumentoRepository.findByName(dto.tipoDocumento())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de documento no encontrado: " + dto.tipoDocumento()));

        User user = userDtoMapper.toEntity(dto, rol, tipoDoc);
        User saved = userRepository.save(user);

        return userDtoMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(String obfuscatedId, CreateUserDto dto) {

        User existing = getUserOrThrow(obfuscatedId);

        if (!existing.getUsername().equals(dto.username()) && userRepository.existsByUsername(dto.username())) {
            throw new IllegalStateException("Ya existe otro usuario con nombre '" + dto.username() + "'");
        }

        long roleId = idEncryptionService.decrypt(dto.role());
        Role rol = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + dto.role()));

        existing.setUsername(dto.username());
        existing.setNombres(dto.nombres());
        existing.setApellidoPaterno(dto.apellidoPaterno());
        existing.setApellidoMaterno(dto.apellidoMaterno());
        existing.setTelefono(dto.telefono());
        existing.setDireccion(dto.direccion());
        existing.setEmail(dto.email());
        existing.setRole(rol);

        if (dto.fechaNacimiento() != null && !dto.fechaNacimiento().isEmpty()) {
            try {
                // Forma moderna y correcta: String (yyyy-MM-dd) -> LocalDate
                existing.setFechaNacimiento(LocalDate.parse(dto.fechaNacimiento()));
            } catch (Exception e) {
                // DateTimeParseException
                throw new RuntimeException("Formato de fecha inválido. Usa yyyy-MM-dd", e);
            }
        }

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
        long id = idEncryptionService.decrypt(obfuscatedId);
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User no encontrado"));
    }
}
