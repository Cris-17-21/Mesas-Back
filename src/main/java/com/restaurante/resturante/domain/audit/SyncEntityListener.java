package com.restaurante.resturante.domain.audit;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Table;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.restaurante.resturante.config.SpringContextHelper;
import com.restaurante.resturante.service.sincronizacion.SyncContext;
import com.restaurante.resturante.service.sincronizacion.SyncQueueService;

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
                final String finalId = id;
                final String finalTableName = tableName;

                try {
                    // Si hay una transacción activa, encolamos el registro justo después del commit exitoso
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    SyncQueueService queueService = SpringContextHelper.getBean(SyncQueueService.class);
                                    queueService.queue(finalTableName, finalId);
                                } catch (Exception e) {
                                    System.err.println("Error encolando sincronización en afterCommit: " + e.getMessage());
                                }
                            }
                        });
                    } else {
                        // Si no hay transacción activa (ej. scripts o seeder directo), ejecutamos de inmediato
                        SyncQueueService queueService = SpringContextHelper.getBean(SyncQueueService.class);
                        queueService.queue(finalTableName, finalId);
                    }
                } catch (Exception e) {
                    System.err.println("Error al registrar sincronización de transacción: " + e.getMessage());
                }
            }
        }
    }
}
