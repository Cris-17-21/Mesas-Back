package com.restaurante.resturante.service.sincronizacion;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.sincronizacion.RegistroSincronizacion;
import com.restaurante.resturante.repository.sincronizacion.RegistroSincronizacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncQueueService {

    private final RegistroSincronizacionRepository syncRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void queue(String tableName, String recordId) {
        try {
            RegistroSincronizacion reg = new RegistroSincronizacion();
            reg.setTablaNombre(tableName);
            reg.setRegistroId(recordId);
            reg.setEstado("PENDIENTE");
            reg.setFechaModificacion(LocalDateTime.now());
            syncRepo.save(reg);
            log.debug("📝 Registrada sincronización pendiente para tabla: {}, ID: {}", tableName, recordId);
        } catch (Exception e) {
            log.error("❌ Error al registrar sincronización para tabla {}: {}", tableName, e.getMessage());
        }
    }
}
