package com.restaurante.resturante.config;

import java.time.LocalDate;
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
import static com.restaurante.resturante.domain.security.enums.Sexo.MASCULINO;
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

        PermissionModule dashboardModule = createModuleIfNotExists("Dashboard", "/dashboard", "heroHome", 0, null);
        createPermissionIfNotExists("VIEW_DASHBOARD", "Ver el dashboard", dashboardModule);

        PermissionModule configModule = createModuleIfNotExists("Configuración", "/config", "heroCog6Tooth", 1, null);
        // Módulos hijo
        PermissionModule modulosModule = createModuleIfNotExists("Módulos", "/modules", "heroFire", 0, configModule);

        createPermissionIfNotExists("READ_MODULE", "Ver módulos", modulosModule);
        createPermissionIfNotExists("CREATE_MODULE", "Crear nuevos módulos", modulosModule);
        createPermissionIfNotExists("UPDATE_MODULE", "Editar módulos", modulosModule);
        createPermissionIfNotExists("DELETE_MODULE", "Eliminar módulos", modulosModule);

        PermissionModule permisosModule = createModuleIfNotExists("Permisos", "/permission", "heroArrowRightEndOnRectangle", 0, configModule);

        createPermissionIfNotExists("READ_PERMISSION", "Ver permisos", permisosModule);
        createPermissionIfNotExists("CREATE_PERMISSION", "Crear nuevos permisos", permisosModule);
        createPermissionIfNotExists("UPDATE_PERMISSION", "Editar permisos", permisosModule);
        createPermissionIfNotExists("DELETE_PERMISSION", "Eliminar permisos", permisosModule);

        PermissionModule rolesModule = createModuleIfNotExists("Roles", "/roles", "heroShieldCheck", 2, configModule);
        PermissionModule userModule = createModuleIfNotExists("Gestión de Usuarios", "/users", "heroUser", 3, configModule);

        // Módulo padre "Maestros"
        PermissionModule maestroModule = createModuleIfNotExists("Maestros", "/maestro", "heroWrench", 2, null);
        // Módulos hijo 
        PermissionModule zonasModule = createModuleIfNotExists("Zonas", "/zonas", "heroMapPin", 1, maestroModule);
        PermissionModule almacenesModule = createModuleIfNotExists("Almacenes", "/almacen", "heroArchiveBox", 2, maestroModule);
        PermissionModule clientesModule = createModuleIfNotExists("Clientes", "/cliente", "heroUsers", 3, maestroModule);
        PermissionModule repuestosModule = createModuleIfNotExists("Repuestos", "/repuesto", "heroPuzzlePiece", 4, maestroModule);
        PermissionModule equiposModule = createModuleIfNotExists("Equipos", "/equipo", "heroComputerDesktop", 5, maestroModule);

        // Permisos de Configuración -> Roles
        createPermissionIfNotExists("READ_ROLE", "Ver roles y permisos", rolesModule);
        createPermissionIfNotExists("CREATE_ROLE", "Crear nuevos roles", rolesModule);
        createPermissionIfNotExists("UPDATE_ROLE", "Editar roles", rolesModule);
        createPermissionIfNotExists("DELETE_ROLE", "Eliminar roles", rolesModule);

        // Permisos de Configuración -> Usuarios
        createPermissionIfNotExists("CREATE_USER", "Crear usuarios", userModule);
        createPermissionIfNotExists("READ_USER", "Ver usuarios", userModule);
        createPermissionIfNotExists("UPDATE_USER", "Editar usuarios", userModule);

        // Permisos de Maestros -> Zonas
        createPermissionIfNotExists("READ_ZONA", "Ver zonas", zonasModule);
        createPermissionIfNotExists("CREATE_ZONA", "Crear zonas", zonasModule);
        createPermissionIfNotExists("UPDATE_ZONA", "Editar zonas", zonasModule);
        createPermissionIfNotExists("DELETE_ZONA", "Eliminar zonas", zonasModule);

        // Permisos de Maestros -> Almacenes
        createPermissionIfNotExists("READ_ALMACEN", "Ver almacenes", almacenesModule);
        createPermissionIfNotExists("CREATE_ALMACEN", "Crear almacenes", almacenesModule);
        createPermissionIfNotExists("UPDATE_ALMACEN", "Editar almacenes", almacenesModule);
        createPermissionIfNotExists("DELETE_ALMACEN", "Eliminar almacenes", almacenesModule);
        createPermissionIfNotExists("READ_TIPO_ALMACEN", "Ver almacenes", almacenesModule);

        // Permisos de Maestros -> Clientes
        createPermissionIfNotExists("READ_CLIENTE", "Ver clientes", clientesModule);
        createPermissionIfNotExists("CREATE_CLIENTE", "Crear clientes", clientesModule);
        createPermissionIfNotExists("UPDATE_CLIENTE", "Editar clientes", clientesModule);
        createPermissionIfNotExists("DELETE_CLIENTE", "Eliminar clientes", clientesModule);

        // Permisos de Maestros -> Repuestos
        createPermissionIfNotExists("READ_REPUESTO", "Ver repuestos", repuestosModule);
        createPermissionIfNotExists("CREATE_REPUESTO", "Crear repuestos", repuestosModule);
        createPermissionIfNotExists("UPDATE_REPUESTO", "Editar repuestos", repuestosModule);
        createPermissionIfNotExists("DELETE_REPUESTO", "Eliminar repuestos", repuestosModule);
        createPermissionIfNotExists("READ_ESTADO_REPUESTO", "Ver repuestos", repuestosModule);

        // Permisos de Maestros -> Equipos
        createPermissionIfNotExists("READ_EQUIPO", "Ver equipos", equiposModule);
        createPermissionIfNotExists("CREATE_EQUIPO", "Crear equipos", equiposModule);
        createPermissionIfNotExists("UPDATE_EQUIPO", "Editar equipos", equiposModule);
        createPermissionIfNotExists("DELETE_EQUIPO", "Eliminar equipos", equiposModule);

        // --- 3. Crear el Rol "SUPER_ADMIN" y asignarle los permisos de prueba ---
        if(roleRepository.findByName("ROLE_SUPER_ADMIN").isEmpty()) {
            Role superAdminRole = new Role();
            Set<Permission> superAdminPermissions = new HashSet<>(permissionRepository.findAll());
            superAdminRole.setName("ROLE_SUPER_ADMIN");
            superAdminRole.setDescription("Super Administrador");
            superAdminRole.setPermissions(superAdminPermissions);
            
            // CORRECCIÓN: Asignar auditoría manualmente
            superAdminRole.setCreatedBy("SYSTEM");

            roleRepository.save(superAdminRole);
        }

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
                .sexo(MASCULINO)
                .fechaNacimiento(LocalDate.now())
                .direccion("Av. Siempre Viva 742")
                .telefono("943316756")
                .email("superadmin@sgrncr.com")
                .active(true)
                .role(rolSuperAdmin)
                .build();
        
        // CORRECCIÓN: Si User extiende de una clase base y el builder no lo cubre, setters manuales:
        superAdminUser.setCreatedBy("SYSTEM");

        userRepository.save(superAdminUser);
        
        System.out.println("✅ DataSeeder finalizado correctamente.");
    }

    private PermissionModule createModuleIfNotExists(String name, String relativePath, String iconName, int order, PermissionModule parent) {
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