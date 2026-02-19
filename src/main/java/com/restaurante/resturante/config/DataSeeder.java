package com.restaurante.resturante.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.repository.security.PermissionModuleRepository;
import com.restaurante.resturante.repository.security.PermissionRepository;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.repository.security.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionModuleRepository permissionModuleRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // La ejecución se detiene si el usuario superadmin ya existe.
        if (userRepository.findByUsername("superadmin").isPresent()) {
            return;
        }

        System.out.println("🌱 Iniciando DataSeeder...");

        PermissionModule dashboardModule = createModuleIfNotExists("Dashboard", "/dashboard", "bi bi-house", 0, null);
        createPermissionIfNotExists("VIEW_DASHBOARD", "Ver el dashboard", dashboardModule);

        PermissionModule configModule = createModuleIfNotExists("Configuración", "/config", "bi bi-gear", 1, null);
        // Módulos hijo
        PermissionModule modulosModule = createModuleIfNotExists("Módulos", "/modules", "bi bi-fire", 0, configModule);

        createPermissionIfNotExists("READ_MODULE", "Ver módulos", modulosModule);
        createPermissionIfNotExists("CREATE_MODULE", "Crear nuevos módulos", modulosModule);
        createPermissionIfNotExists("UPDATE_MODULE", "Editar módulos", modulosModule);
        createPermissionIfNotExists("DELETE_MODULE", "Eliminar módulos", modulosModule);

        PermissionModule permisosModule = createModuleIfNotExists("Permisos", "/permission", "bi bi-shield-lock", 0,
                configModule);

        createPermissionIfNotExists("READ_PERMISSION", "Ver permisos", permisosModule);
        createPermissionIfNotExists("CREATE_PERMISSION", "Crear nuevos permisos", permisosModule);
        createPermissionIfNotExists("UPDATE_PERMISSION", "Editar permisos", permisosModule);
        createPermissionIfNotExists("DELETE_PERMISSION", "Eliminar permisos", permisosModule);

        PermissionModule rolesModule = createModuleIfNotExists("Roles", "/roles", "bi bi-shield-check", 2,
                configModule);
        PermissionModule userModule = createModuleIfNotExists("Usuarios", "/users", "bi bi-people", 3, configModule);

        // Permisos de Configuración -> Roles
        createPermissionIfNotExists("READ_ROLE", "Ver roles y permisos", rolesModule);
        createPermissionIfNotExists("CREATE_ROLE", "Crear nuevos roles", rolesModule);
        createPermissionIfNotExists("UPDATE_ROLE", "Editar roles", rolesModule);
        createPermissionIfNotExists("DELETE_ROLE", "Eliminar roles", rolesModule);

        // Permisos de Configuración -> Usuarios
        createPermissionIfNotExists("CREATE_USER", "Crear usuarios", userModule);
        createPermissionIfNotExists("READ_USER", "Ver usuarios", userModule);
        createPermissionIfNotExists("UPDATE_USER", "Editar usuarios", userModule);

        // Modulo de creacion de maestros
        PermissionModule maestrosModule = createModuleIfNotExists("Maestros", "/maestros", "bi bi-wrench-adjustable", 2,
                null);
        PermissionModule empresaModule = createModuleIfNotExists("Empresas", "/empresas", "bi bi-building", 1,
                maestrosModule);
        PermissionModule sucursalesModule = createModuleIfNotExists("Sucursales", "/sucursales", "bi bi-buildings", 2,
                maestrosModule);

        // Permisos de Maestros -> empresas
        // Permisos de Maestros -> empresas
        createPermissionIfNotExists("READ_EMPRESA", "Ver empresas", empresaModule);
        createPermissionIfNotExists("CREATE_EMPRESA", "Crear empresas", empresaModule);
        createPermissionIfNotExists("UPDATE_EMPRESA", "Editar empresas", empresaModule);
        createPermissionIfNotExists("DELETE_EMPRESA", "Eliminar empresas", empresaModule);

        // Permisos de Maestros -> sucursales
        createPermissionIfNotExists("READ_SUCURSAL", "Ver sucursales", sucursalesModule);
        createPermissionIfNotExists("CREATE_SUCURSAL", "Crear sucursales", sucursalesModule);
        createPermissionIfNotExists("UPDATE_SUCURSAL", "Editar sucursales", sucursalesModule);
        createPermissionIfNotExists("DELETE_SUCURSAL", "Eliminar sucursales", sucursalesModule);

        // --- MÓDULO DE COMPRAS (Padre) ---
        PermissionModule comprasModule = createModuleIfNotExists("Compras", "/compras", "bi bi-cart4", 4, null);

        // 1. Compras -> Productos
        PermissionModule productosModule = createModuleIfNotExists("Productos", "/productos", "bi bi-box-seam", 1,
                comprasModule);
        createPermissionIfNotExists("READ_PRODUCTO", "Ver productos", productosModule);
        createPermissionIfNotExists("CREATE_PRODUCTO", "Crear productos", productosModule);
        createPermissionIfNotExists("UPDATE_PRODUCTO", "Editar productos", productosModule);
        createPermissionIfNotExists("DELETE_PRODUCTO", "Eliminar productos", productosModule);

        // 2. Compras -> Gestión de Compras
        PermissionModule pedidosCompraModule = createModuleIfNotExists("Gestión de Compras", "/pedidos",
                "bi bi-receipt", 2, comprasModule);
        createPermissionIfNotExists("READ_COMPRA", "Ver compras", pedidosCompraModule);
        createPermissionIfNotExists("CREATE_COMPRA", "Registrar compra", pedidosCompraModule);
        createPermissionIfNotExists("UPDATE_COMPRA", "Editar compra", pedidosCompraModule);
        createPermissionIfNotExists("DELETE_COMPRA", "Anular compra", pedidosCompraModule);

        // 3. Compras -> Proveedores
        PermissionModule proveedoresModule = createModuleIfNotExists("Proveedores", "/proveedores", "bi bi-truck", 3,
                comprasModule);
        createPermissionIfNotExists("READ_PROVEEDOR", "Ver proveedores", proveedoresModule);
        createPermissionIfNotExists("CREATE_PROVEEDOR", "Crear proveedores", proveedoresModule);
        createPermissionIfNotExists("UPDATE_PROVEEDOR", "Editar proveedores", proveedoresModule);
        createPermissionIfNotExists("DELETE_PROVEEDOR", "Eliminar proveedores", proveedoresModule);
        // Modulo de creacion de ventas
        PermissionModule ventasModule = createModuleIfNotExists("Ventas", "/ventas", "null", 3, null);
        PermissionModule comandaModule = createModuleIfNotExists("Pedido", "/pedido", "null", 1, ventasModule);
        createPermissionIfNotExists("READ_PEDIDO", "Ver pedido", comandaModule);

        // --- 3. Crear o Actualizar el Rol "SUPER_ADMIN" y asignarle TODOS los permisos
        // ---
        // Buscamos si existe, si no, lo creamos.
        Role superAdminRole = roleRepository.findByName("ROLE_SUPER_ADMIN").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_SUPER_ADMIN");
            newRole.setDescription("Super Administrador");
            newRole.setCreatedBy("SYSTEM");
            return newRole;
        });

        // SIEMPRE actualizamos los permisos al reiniciar
        Set<Permission> superAdminPermissions = new HashSet<>(permissionRepository.findAll());
        superAdminRole.setPermissions(superAdminPermissions);
        roleRepository.save(superAdminRole);

        // --- Crear Tipo Documento ---
        TipoDocumento dniTipo = tipoDocumentoRepository.findByName("DNI").orElseGet(() -> {
            TipoDocumento nuevo = new TipoDocumento();
            nuevo.setName("DNI");
            // CORRECCIÓN: Asignar auditoría manualmente
            nuevo.setCreatedBy("SYSTEM");
            return tipoDocumentoRepository.save(nuevo);
        });

        // --- 4. Crear el Usuario Super Admin ---
        Role rolSuperAdmin = roleRepository.findByName("ROLE_SUPER_ADMIN").orElseThrow();

        User superAdminUser = User.builder()
                .username("superadmin")
                .password(passwordEncoder.encode("admin123"))
                .nombres("Super")
                .apellidoPaterno("Admin")
                .apellidoMaterno("User")
                .tipoDocumento(dniTipo)
                .numeroDocumento("00000000")
                .telefono("943316756")
                .email("superadmin@sgrncr.com")
                .active(true)
                .role(rolSuperAdmin)
                .build();

        // CORRECCIÓN: Si User extiende de una clase base y el builder no lo cubre,
        // setters manuales:
        superAdminUser.setCreatedBy("SYSTEM");

        userRepository.save(superAdminUser);

        System.out.println("✅ DataSeeder finalizado correctamente.");
    }

    private PermissionModule createModuleIfNotExists(String name, String relativePath, String iconName, int order,
            PermissionModule parent) {
        return permissionModuleRepository.findByName(name).orElseGet(() -> {
            PermissionModule newModule = new PermissionModule();
            newModule.setName(name);
            newModule.setIconName(iconName);
            newModule.setDisplayOrder(order);
            newModule.setParent(parent);

            String finalPath = relativePath;
            if (parent != null && parent.getUrlPath() != null && relativePath != null) {
                finalPath = parent.getUrlPath() + relativePath;
            }
            newModule.setUrlPath(finalPath);

            return permissionModuleRepository.save(newModule);
        });
    }

    private Permission createPermissionIfNotExists(String name, String description, PermissionModule module) {
        return permissionRepository.findByName(name).orElseGet(() -> {
            Permission newPermission = new Permission();
            newPermission.setName(name);
            newPermission.setDescription(description);
            newPermission.setModule(module);

            // CORRECCIÓN: Auditoría manual
            newPermission.setCreatedBy("SYSTEM");

            return permissionRepository.save(newPermission);
        });
    }
}