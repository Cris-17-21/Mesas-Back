package com.restaurante.resturante.domain.audit;

import java.time.LocalDateTime;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Table;

import com.restaurante.resturante.config.SpringContextHelper;
import com.restaurante.resturante.domain.sincronizacion.RegistroSincronizacion;
import com.restaurante.resturante.repository.sincronizacion.RegistroSincronizacionRepository;
import com.restaurante.resturante.service.sincronizacion.SyncContext;

public class SyncEntityListener {

    @PostPersist
    @PostUpdate
    public void queueSync(Object entity) {
        // Si estamos importando datos activamente del pull sync, ignoramos para no generar ciclos recursivos
        if (SyncContext.isSyncing()) {
            return;
        }

        if (entity instanceof Auditable) {
            // Determinar nombre de la tabla o entidad
            String tableName = entity.getClass().getSimpleName().toLowerCase();
            Table tableAnn = entity.getClass().getAnnotation(Table.class);
            if (tableAnn != null && !tableAnn.name().isEmpty()) {
                tableName = tableAnn.name();
            }

            // Obtener el ID de la entidad dinámicamente usando reflexión
            String id = null;
            try {
                // Primero buscamos el método annotated o el campo annotated con @Id
                Class<?> current = entity.getClass();
                while (current != null && current != Object.class) {
                    for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                        if (field.isAnnotationPresent(jakarta.persistence.Id.class) || 
                            field.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                            field.setAccessible(true);
                            Object val = field.get(entity);
                            if (val != null) {
                                id = val.toString();
                            }
                            break;
                        }
                    }
                    if (id != null) break;
                    current = current.getSuperclass();
                }
            } catch (Exception e) {
                // Fallback silencioso
            }

            if (id != null) {
                try {
                    RegistroSincronizacionRepository syncRepo = SpringContextHelper.getBean(RegistroSincronizacionRepository.class);
                    
                    RegistroSincronizacion reg = new RegistroSincronizacion();
                    reg.setTablaNombre(tableName);
                    reg.setRegistroId(id);
                    reg.setEstado("PENDIENTE");
                    reg.setFechaModificacion(LocalDateTime.now());
                    
                    syncRepo.save(reg);
                } catch (Exception e) {
                    // Fail silently para no causar rollback en la transacción de negocio
                    System.err.println("Error encolando sincronización: " + e.getMessage());
                }
            }
        }
    }
}
