package com.restaurante.resturante.service.security.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.MenuModuleDto;
import com.restaurante.resturante.mapper.security.MenuDtoMapper;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.security.IMenuService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService implements IMenuService{

    private final MenuDtoMapper menuDtoMapper;
    private final UserRepository userRepository;

    @Override
    public List<MenuModuleDto> buildUserMenu(User user) {
        // 1. Obtenemos el conjunto de todos los permisos del usuario.
        Set<Permission> userPermissions = user.getRole() != null
                ? user.getRole().getPermissions()
                : Set.of();

        // 2. Obtenemos los módulos "hoja" (donde están los permisos).
        Set<PermissionModule> directModules = userPermissions.stream()
                .map(Permission::getModule)
                .collect(Collectors.toSet());

        // 3. Recolectamos todos los módulos visibles, incluyendo los padres recursivamente.
        Set<PermissionModule> allVisibleModules = new HashSet<>(directModules);
        for (PermissionModule module : directModules) {
            PermissionModule parent = module.getParent();
            while (parent != null) {
                allVisibleModules.add(parent);
                parent = parent.getParent();
            }
        }

        // 4. Pasamos AMBOS conjuntos de datos al mapper para que construya el árbol.
        return menuDtoMapper.buildMenuTree(allVisibleModules, userPermissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuModuleDto> buildUserMenuByUsername(String username) {
        User user = userRepository.findByUsernameWithDetails(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        
        return buildUserMenu(user);
    }
}
