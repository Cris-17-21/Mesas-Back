package com.restaurante.resturante.mapper.security;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.dto.security.MenuModuleDto;
import com.restaurante.resturante.dto.security.MenuPermissionDto;

@Component 
public class MenuDtoMapper {

    /**
     * Método público principal para construir el árbol de menú.
     * @param allVisibleModules El conjunto de TODOS los módulos (padres e hijos) a los que el usuario tiene acceso.
     * @param userPermissions El conjunto de TODOS los permisos específicos del usuario.
     * @return La estructura de menú jerárquica.
     */
    public List<MenuModuleDto> buildMenuTree(Set<PermissionModule> allVisibleModules, Set<Permission> userPermissions) {
        // Filtramos para obtener solo los módulos raíz (sin padre) y los ordenamos.
        List<PermissionModule> rootModules = allVisibleModules.stream()
                .filter(module -> module.getParent() == null)
                .sorted(Comparator.comparingInt(PermissionModule::getDisplayOrder))
                .collect(Collectors.toList());

        // Para cada módulo raíz, construimos su sub-árbol de forma recursiva.
        return rootModules.stream()
                .map(root -> buildMenuNode(root, allVisibleModules, userPermissions))
                .collect(Collectors.toList());
    }

    /**
     * Método recursivo privado que construye cada nodo (módulo) del árbol.
     */
    private MenuModuleDto buildMenuNode(PermissionModule module, Set<PermissionModule> allModules, Set<Permission> userPermissions) {
        // 1. Filtramos la lista COMPLETA de permisos del usuario para encontrar solo los de ESTE módulo.
        List<MenuPermissionDto> permissionsForThisModule = userPermissions.stream()
                .filter(permission -> permission.getModule().getId().equals(module.getId()))
                .map(permission -> new MenuPermissionDto(permission.getId(), permission.getName()))
                .collect(Collectors.toList());

        // 2. Buscamos y construimos recursivamente los hijos de este módulo.
        List<MenuModuleDto> children = allModules.stream()
                .filter(child -> child.getParent() != null && child.getParent().getId().equals(module.getId()))
                .sorted(Comparator.comparingInt(PermissionModule::getDisplayOrder))
                .map(child -> buildMenuNode(child, allModules, userPermissions))
                .collect(Collectors.toList());

        // 3. Devolvemos el DTO completo, ahora incluyendo los permisos correctos.
        return new MenuModuleDto(
                module.getName(),
                String.valueOf(module.getDisplayOrder()),
                module.getUrlPath(),
                module.getIconName(),
                permissionsForThisModule, // <-- ¡PERMISOS INCLUIDOS!
                children
        );
    }
}
