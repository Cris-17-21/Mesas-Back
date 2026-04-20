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

import com.restaurante.resturante.domain.compras.TiposPago;
import com.restaurante.resturante.repository.compras.TiposPagoRepository;

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
        private final TiposPagoRepository tiposPagoRepository;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                // Siempre seedear tipos de pago (idempotente)
                seedTiposPago();

                // El resto solo se ejecuta la primera vez (cuando no existe superadmin)
                // Pero permitimos que siga para que se actualicen los módulos y permisos
                boolean isFirstRun = userRepository.findByUsername("superadmin").isEmpty();
                
                System.out.println("🌱 Sincronizando módulos y permisos...");

                PermissionModule dashboardModule = createModuleIfNotExists("Dashboard", "/dashboard", "bi bi-house", 0,
                                null);
                createPermissionIfNotExists("VIEW_DASHBOARD", "Ver el dashboard", dashboardModule);

                PermissionModule configModule = createModuleIfNotExists("Configuración", "/config", "bi bi-gear", 1,
                                null);
                // Módulos hijo
                PermissionModule modulosModule = createModuleIfNotExists("Módulos", "/modules", "bi bi-fire", 0,
                                configModule);

                createPermissionIfNotExists("READ_MODULE", "Ver módulos", modulosModule);
                createPermissionIfNotExists("CREATE_MODULE", "Crear nuevos módulos", modulosModule);
                createPermissionIfNotExists("UPDATE_MODULE", "Editar módulos", modulosModule);
                createPermissionIfNotExists("DELETE_MODULE", "Eliminar módulos", modulosModule);

                PermissionModule permisosModule = createModuleIfNotExists("Permisos", "/permission",
                                "bi bi-shield-lock", 0,
                                configModule);

                createPermissionIfNotExists("READ_PERMISSION", "Ver permisos", permisosModule);
                createPermissionIfNotExists("CREATE_PERMISSION", "Crear nuevos permisos", permisosModule);
                createPermissionIfNotExists("UPDATE_PERMISSION", "Editar permisos", permisosModule);
                createPermissionIfNotExists("DELETE_PERMISSION", "Eliminar permisos", permisosModule);

                PermissionModule rolesModule = createModuleIfNotExists("Roles", "/roles", "bi bi-shield-check", 2,
                                configModule);
                PermissionModule userModule = createModuleIfNotExists("Usuarios", "/users", "bi bi-people", 3,
                                configModule);

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
                PermissionModule maestrosModule = createModuleIfNotExists("Maestros", "/maestros",
                                "bi bi-wrench-adjustable", 2,
                                null);
                PermissionModule empresaModule = createModuleIfNotExists("Empresas", "/empresa", "bi bi-building", 1,
                                maestrosModule);
                PermissionModule sucursalesModule = createModuleIfNotExists("Sucursales", "/sucursal",
                                "bi bi-buildings", 2,
                                maestrosModule);

                PermissionModule pisosModule = createModuleIfNotExists("Pisos", "/pisos", "bi bi-layers", 3,
                                maestrosModule);
                PermissionModule mesasModule = createModuleIfNotExists("Mesas", "/mesas", "bi bi-grid-3x3-gap", 4,
                                maestrosModule);
                PermissionModule clientesModule = createModuleIfNotExists("Clientes", "/clientes", "bi bi-person-lines-fill", 5,
                                maestrosModule);
                PermissionModule metodosModule = createModuleIfNotExists("Métodos de Pago", "/metodos", "bi bi-credit-card", 6,
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

                // Permisos de Maestros -> Otros (CRUD Completo)
                createPermissionIfNotExists("READ_PISO", "Ver pisos", pisosModule);
                createPermissionIfNotExists("CREATE_PISO", "Crear pisos", pisosModule);
                createPermissionIfNotExists("UPDATE_PISO", "Editar pisos", pisosModule);
                createPermissionIfNotExists("DELETE_PISO", "Eliminar pisos", pisosModule);

                createPermissionIfNotExists("READ_MESA", "Ver mesas", mesasModule);
                createPermissionIfNotExists("CREATE_MESA", "Crear mesas", mesasModule);
                createPermissionIfNotExists("UPDATE_MESA", "Editar mesas", mesasModule);
                createPermissionIfNotExists("DELETE_MESA", "Eliminar mesas", mesasModule);

                createPermissionIfNotExists("READ_CLIENTE", "Ver clientes", clientesModule);
                createPermissionIfNotExists("CREATE_CLIENTE", "Crear clientes", clientesModule);
                createPermissionIfNotExists("UPDATE_CLIENTE", "Editar clientes", clientesModule);
                createPermissionIfNotExists("DELETE_CLIENTE", "Eliminar clientes", clientesModule);

                createPermissionIfNotExists("READ_METODO_PAGO", "Ver métodos de pago", metodosModule);
                createPermissionIfNotExists("CREATE_METODO_PAGO", "Crear métodos de pago", metodosModule);
                createPermissionIfNotExists("UPDATE_METODO_PAGO", "Editar métodos de pago", metodosModule);
                createPermissionIfNotExists("DELETE_METODO_PAGO", "Eliminar métodos de pago", metodosModule);

                // --- MÓDULO DE COMPRAS (Padre) ---
                PermissionModule comprasModule = createModuleIfNotExists("Compras", "/compras", "bi bi-cart4", 4, null);

                // Under Compras Module
                PermissionModule clasificacionModule = createModuleIfNotExists("Clasificación", "/clasificacion",
                                "bi bi-tags", 2, comprasModule); // Order 2, bump others
                // Categoria
                createPermissionIfNotExists("READ_CATEGORIA", "Ver categorías", clasificacionModule);
                createPermissionIfNotExists("CREATE_CATEGORIA", "Crear categorías", clasificacionModule);
                createPermissionIfNotExists("UPDATE_CATEGORIA", "Editar categorías", clasificacionModule);
                createPermissionIfNotExists("DELETE_CATEGORIA", "Eliminar categorías", clasificacionModule);
                // Tipo Producto
                createPermissionIfNotExists("READ_TIPOPRODUCTO", "Ver tipos de producto", clasificacionModule);
                createPermissionIfNotExists("CREATE_TIPOPRODUCTO", "Crear tipos de producto", clasificacionModule);
                createPermissionIfNotExists("UPDATE_TIPOPRODUCTO", "Editar tipos de producto", clasificacionModule);
                createPermissionIfNotExists("DELETE_TIPOPRODUCTO", "Eliminar tipos de producto", clasificacionModule);

                // 1. Compras -> Productos
                PermissionModule productosModule = createModuleIfNotExists("Productos", "/productos", "bi bi-box-seam",
                                1,
                                comprasModule);
                createPermissionIfNotExists("READ_PRODUCTO", "Ver productos", productosModule);
                createPermissionIfNotExists("CREATE_PRODUCTO", "Crear productos", productosModule);
                createPermissionIfNotExists("UPDATE_PRODUCTO", "Editar productos", productosModule);
                createPermissionIfNotExists("DELETE_PRODUCTO", "Eliminar productos", productosModule);
                
                // Permisos específicos para Platos (dentro de Productos)
                createPermissionIfNotExists("CREATE_PLATO", "Agregar nuevos platos", productosModule);
                createPermissionIfNotExists("SELL_PLATO", "Vender platos", productosModule);
                createPermissionIfNotExists("VIEW_PLATO_SALES", "Ver cantidad vendida de platos", productosModule);

                // 2. Compras -> Gestión de Compras
                PermissionModule pedidosCompraModule = createModuleIfNotExists("Gestión de Compras", "/gestion",
                                "bi bi-receipt", 2, comprasModule);
                createPermissionIfNotExists("READ_COMPRA", "Ver compras", pedidosCompraModule);
                createPermissionIfNotExists("CREATE_COMPRA", "Registrar compra", pedidosCompraModule);
                createPermissionIfNotExists("UPDATE_COMPRA", "Editar compra", pedidosCompraModule);
                createPermissionIfNotExists("DELETE_COMPRA", "Anular compra", pedidosCompraModule);

                // 3. Compras -> Proveedores
                PermissionModule proveedoresModule = createModuleIfNotExists("Proveedores", "/proveedores",
                                "bi bi-truck", 3,
                                comprasModule);
                createPermissionIfNotExists("READ_PROVEEDOR", "Ver proveedores", proveedoresModule);
                createPermissionIfNotExists("CREATE_PROVEEDOR", "Crear proveedores", proveedoresModule);
                createPermissionIfNotExists("UPDATE_PROVEEDOR", "Editar proveedores", proveedoresModule);
                createPermissionIfNotExists("DELETE_PROVEEDOR", "Eliminar proveedores", proveedoresModule);
                // Modulo de creacion de ventas
                PermissionModule ventasModule = createModuleIfNotExists("Ventas", "/ventas", "null", 3, null);
                PermissionModule comandaModule = createModuleIfNotExists("Pedido", "/pedido", "null", 1, ventasModule);
                createPermissionIfNotExists("READ_PEDIDO", "Ver pedido", comandaModule);

                // --- 4. Almacén / Inventario ---
                PermissionModule almacenModule = createModuleIfNotExists("Almacén", "/almacen", "bi bi-archive", 5,
                                null);
                PermissionModule inventarioModule = createModuleIfNotExists("Inventario", "/inventario",
                                "bi bi-journal-text",
                                1, almacenModule);

                createPermissionIfNotExists("VER_INVENTARIO", "Ver stock de inventario", inventarioModule);
                createPermissionIfNotExists("READ_INVENTARIO", "Leer datos de inventario", inventarioModule);

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

                // SIEMPRE actualizamos los permisos al reiniciar para incluir los nuevos
                // Pero filtramos los que no debe tener el SuperAdmin (Pisos, Mesas, Métodos de Pago)
                Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
                Set<Permission> superAdminPermissions = allPermissions.stream()
                        .filter(p -> !p.getName().contains("_PISO") && 
                                     !p.getName().contains("_MESA") && 
                                     !p.getName().contains("_METODO_PAGO"))
                        .collect(java.util.stream.Collectors.toSet());
                
                superAdminRole.setPermissions(superAdminPermissions);
                roleRepository.save(superAdminRole);

                // --- Crear Tipo Documento ---
                TipoDocumento dniTipo = tipoDocumentoRepository.findByName("DNI").orElseGet(() -> {
                        TipoDocumento nuevo = new TipoDocumento();
                        nuevo.setName("DNI");
                        nuevo.setCreatedBy("SYSTEM");
                        return tipoDocumentoRepository.save(nuevo);
                });

                // --- 4. Crear el Usuario Super Admin (solo si no existe) ---
                if (isFirstRun) {
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

                        superAdminUser.setCreatedBy("SYSTEM");
                        userRepository.save(superAdminUser);
                }

                System.out.println("✅ DataSeeder finalizado correctamente.");
        }

        private PermissionModule createModuleIfNotExists(String name, String relativePath, String iconName, int order,
                        PermissionModule parent) {
                
                String finalPath = relativePath;
                if (parent != null && parent.getUrlPath() != null && relativePath != null) {
                        finalPath = parent.getUrlPath() + relativePath;
                }
                final String urlPath = finalPath;

                return permissionModuleRepository.findByName(name).map(existingModule -> {
                        // Si ya existe, verificamos si la ruta ha cambiado
                        if (!urlPath.equals(existingModule.getUrlPath())) {
                                System.out.println("🔄 Actualizando ruta de " + name + ": " + existingModule.getUrlPath() + " -> " + urlPath);
                                existingModule.setUrlPath(urlPath);
                                return permissionModuleRepository.save(existingModule);
                        }
                        return existingModule;
                }).orElseGet(() -> {
                        PermissionModule newModule = new PermissionModule();
                        newModule.setName(name);
                        newModule.setIconName(iconName);
                        newModule.setDisplayOrder(order);
                        newModule.setParent(parent);
                        newModule.setUrlPath(urlPath);
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

        private void seedTiposPago() {
                crearTipoPagoSiNoExiste(1, "Efectivo");
                crearTipoPagoSiNoExiste(2, "Transferencia Bancaria");
                crearTipoPagoSiNoExiste(3, "Cheque");
                crearTipoPagoSiNoExiste(4, "Tarjeta de Crédito");
                crearTipoPagoSiNoExiste(5, "Tarjeta de Débito");
                crearTipoPagoSiNoExiste(6, "Crédito 30 días");
                crearTipoPagoSiNoExiste(7, "Crédito 60 días");
                crearTipoPagoSiNoExiste(8, "Crédito 90 días");
        }

        private void crearTipoPagoSiNoExiste(Integer id, String nombre) {
                if (!tiposPagoRepository.existsById(id)) {
                        TiposPago tp = new TiposPago();
                        tp.setIdTipoPago(id);
                        tp.setTipoPago(nombre);
                        tiposPagoRepository.save(tp);
                }
        }
}
