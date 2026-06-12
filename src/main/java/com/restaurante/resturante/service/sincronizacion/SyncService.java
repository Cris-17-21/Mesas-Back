package com.restaurante.resturante.service.sincronizacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.compras.TiposPago;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.repository.compras.TiposPagoRepository;
import com.restaurante.resturante.repository.inventario.CategoriaProductoRepository;
import com.restaurante.resturante.repository.inventario.ProductoRepository;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.MedioPagoRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.PermissionModuleRepository;
import com.restaurante.resturante.repository.security.PermissionRepository;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;

    // Tabla -> Clase de Entidad
    private static final Map<String, Class<?>> tableToEntityClass = Map.ofEntries(
        Map.entry("empresas", Empresa.class),
        Map.entry("sucursales", Sucursal.class),
        Map.entry("productos", Producto.class),
        Map.entry("categoriasproducto", CategoriaProducto.class),
        Map.entry("users", User.class),
        Map.entry("roles", Role.class),
        Map.entry("permissions", Permission.class),
        Map.entry("permission_modules", PermissionModule.class),
        Map.entry("user_accesses", UserAccess.class),
        Map.entry("tipo_documentos", TipoDocumento.class),
        Map.entry("tipos_pago", TiposPago.class),
        Map.entry("medios_pago", MedioPago.class)
    );

    // Clase de Entidad -> Clase de Repositorio
    private static final Map<Class<?>, Class<? extends JpaRepository<?, ?>>> entityToRepoClass = Map.ofEntries(
        Map.entry(Empresa.class, EmpresaRepository.class),
        Map.entry(Sucursal.class, SucursalRepository.class),
        Map.entry(Producto.class, ProductoRepository.class),
        Map.entry(CategoriaProducto.class, CategoriaProductoRepository.class),
        Map.entry(User.class, UserRepository.class),
        Map.entry(Role.class, RoleRepository.class),
        Map.entry(Permission.class, PermissionRepository.class),
        Map.entry(PermissionModule.class, PermissionModuleRepository.class),
        Map.entry(UserAccess.class, UserAccessRepository.class),
        Map.entry(TipoDocumento.class, TipoDocumentoRepository.class),
        Map.entry(TiposPago.class, TiposPagoRepository.class),
        Map.entry(MedioPago.class, MedioPagoRepository.class)
    );

    public Class<?> getEntityClass(String tableName) {
        return tableToEntityClass.get(tableName.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public JpaRepository<Object, Object> getRepository(Class<?> entityClass) {
        Class<? extends JpaRepository<?, ?>> repoClass = entityToRepoClass.get(entityClass);
        if (repoClass == null) {
            throw new IllegalArgumentException("No repository mapped for entity: " + entityClass.getName());
        }
        return (JpaRepository<Object, Object>) applicationContext.getBean(repoClass);
    }

    @Transactional(readOnly = true)
    public Object findEntity(Class<?> entityClass, Object id) {
        try {
            JpaRepository<Object, Object> repo = getRepository(entityClass);
            
            // Si el ID es entero
            if (id instanceof Number || (id instanceof String && isNumeric((String) id))) {
                Integer intId = Integer.parseInt(id.toString());
                Optional<Object> res = repo.findById(intId);
                if (res.isPresent()) return res.get();
            }
            
            return repo.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("Error finding entity {} with ID {}: {}", entityClass.getSimpleName(), id, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void saveFlatMap(String tableName, Map<String, Object> flatMap) {
        Class<?> entityClass = getEntityClass(tableName);
        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
        
        SyncContext.setSyncing(true);
        try {
            Object entity = mapToEntity(flatMap, entityClass);
            entityManager.merge(entity);
            entityManager.flush();
            log.info("💾 Entidad {} guardada/actualizada con éxito mediante sync.", entityClass.getSimpleName());
        } finally {
            SyncContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> exportMasterData() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        // Exportar cada una de las tablas del maestro
        for (Map.Entry<String, Class<?>> entry : tableToEntityClass.entrySet()) {
            String tableName = entry.getKey();
            Class<?> entityClass = entry.getValue();
            
            JpaRepository<Object, Object> repo = getRepository(entityClass);
            List<Object> entities = repo.findAll();
            
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (Object entity : entities) {
                mapList.add(entityToMap(entity));
            }
            result.put(tableName, mapList);
        }
        
        return result;
    }

    @Transactional
    public void importMasterData(Map<String, List<Map<String, Object>>> data) {
        SyncContext.setSyncing(true);
        try {
            // Orden de inserción para respetar llaves foráneas y evitar FK constraints violations
            List<String> order = List.of(
                "tipo_documentos", "permission_modules", "roles", "permissions",
                "users", "empresas", "sucursales", "user_accesses", "tipos_pago",
                "medios_pago", "categoriasproducto", "productos"
            );

            for (String tableName : order) {
                List<Map<String, Object>> records = data.get(tableName);
                if (records == null || records.isEmpty()) continue;

                Class<?> entityClass = getEntityClass(tableName);
                if (entityClass == null) continue;

                log.info("📥 Importando {} registros para la tabla {}...", records.size(), tableName);
                for (Map<String, Object> record : records) {
                    try {
                        Object entity = mapToEntity(record, entityClass);
                        entityManager.merge(entity);
                    } catch (Exception e) {
                        log.error("❌ Error importando registro en {}: {}", tableName, e.getMessage());
                    }
                }
                entityManager.flush();
            }
        } finally {
            SyncContext.clear();
        }
    }

    // Convertir entidad JPA a Mapa plano (reemplaza relaciones por su ID y excluye colecciones)
    public static Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value == null) {
                        map.put(field.getName(), null);
                        continue;
                    }

                    // Ignorar colecciones o mapas (relaciones OneToMany/ManyToMany perezosas)
                    if (value instanceof java.util.Collection || value instanceof java.util.Map) {
                        continue;
                    }

                    // Si es una relación ManyToOne (otra entidad de JPA)
                    if (value.getClass().isAnnotationPresent(jakarta.persistence.Entity.class)) {
                        Object relId = getEntityId(value);
                        map.put(field.getName() + "Id", relId != null ? relId.toString() : null);
                    } else {
                        map.put(field.getName(), value);
                    }
                } catch (Exception e) {
                    // Ignorar errores en campos inaccesibles
                }
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }

    // Reconstruir entidad JPA a partir de un mapa plano resolviendo relaciones mediante proxies
    public Object mapToEntity(Map<String, Object> map, Class<?> clazz) {
        try {
            Object entity = clazz.getDeclaredConstructor().newInstance();
            Class<?> currentClazz = clazz;
            while (currentClazz != null && currentClazz != Object.class) {
                for (java.lang.reflect.Field field : currentClazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();

                    // Si es una relación ManyToOne
                    if (field.getType().isAnnotationPresent(jakarta.persistence.Entity.class)) {
                        String idKey = fieldName + "Id";
                        if (map.containsKey(idKey)) {
                            Object idVal = map.get(idKey);
                            if (idVal != null) {
                                Object relatedRef = getEntityManagerReference(field.getType(), idVal);
                                field.set(entity, relatedRef);
                            } else {
                                field.set(entity, null);
                            }
                        }
                    } else if (map.containsKey(fieldName)) {
                        Object val = map.get(fieldName);
                        if (val != null) {
                            field.set(entity, convertValue(val, field.getType()));
                        } else {
                            field.set(entity, null);
                        }
                    }
                }
                currentClazz = currentClazz.getSuperclass();
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping flat map to entity " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private Object getEntityManagerReference(Class<?> entityClass, Object idVal) {
        // Si el ID es numérico
        if (idVal instanceof Number || (idVal instanceof String && isNumeric((String) idVal))) {
            Integer intId = Integer.parseInt(idVal.toString());
            return entityManager.getReference(entityClass, intId);
        }
        return entityManager.getReference(entityClass, idVal.toString());
    }

    private static Object getEntityId(Object entity) {
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class) || 
                    field.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                    field.setAccessible(true);
                    try {
                        return field.get(entity);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (targetType == java.math.BigDecimal.class) {
            return new java.math.BigDecimal(value.toString());
        }
        if (targetType == java.time.Instant.class) {
            return java.time.Instant.parse(value.toString());
        }
        if (targetType == java.time.LocalDateTime.class) {
            return java.time.LocalDateTime.parse(value.toString());
        }
        if (targetType == java.time.LocalDate.class) {
            return java.time.LocalDate.parse(value.toString());
        }
        if (targetType == java.lang.Long.class || targetType == long.class) {
            return Long.valueOf(value.toString());
        }
        if (targetType == java.lang.Integer.class || targetType == int.class) {
            return Integer.valueOf(value.toString());
        }
        if (targetType == java.lang.Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value.toString());
        }
        if (targetType == byte[].class && value instanceof String) {
            return java.util.Base64.getDecoder().decode((String) value);
        }
        return value;
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("\\d+");
    }
}
