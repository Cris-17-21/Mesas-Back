package com.restaurante.resturante.service.sincronizacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.restaurante.resturante.domain.sincronizacion.RegistroSincronizacion;
import com.restaurante.resturante.repository.sincronizacion.RegistroSincronizacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SincronizacionCronService {

    private final RegistroSincronizacionRepository syncRepository;
    private final SyncService syncService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sincronizacion.habilitada:false}")
    private boolean habilitada;

    @Value("${sincronizacion.vps.url:http://localhost:8082/api/sync}")
    private String vpsUrl;

    @Value("${sincronizacion.token:default-secret-sync-token}")
    private String syncToken;

    @Scheduled(fixedDelay = 30000) // Cada 30 segundos para Push
    public void procesarSincronizacionPendiente() {
        if (!habilitada) {
            return;
        }

        List<RegistroSincronizacion> pendientes = syncRepository.findByEstado("PENDIENTE");
        if (pendientes.isEmpty()) {
            return;
        }

        log.info("🔄 Se encontraron {} registros pendientes de sincronizar con el VPS...", pendientes.size());

        for (RegistroSincronizacion reg : pendientes) {
            try {
                // Buscar la entidad en la base de datos local
                Class<?> entityClass = syncService.getEntityClass(reg.getTablaNombre());
                if (entityClass == null) {
                    log.warn("⚠️ Tabla desconocida: {}. Marcando como sincronizado para evitar reintentos.", reg.getTablaNombre());
                    reg.setEstado("SINCRONIZADO");
                    reg.setFechaModificacion(LocalDateTime.now());
                    syncRepository.save(reg);
                    continue;
                }

                Object entity = syncService.findEntity(entityClass, reg.getRegistroId());
                if (entity == null) {
                    log.info("ℹ️ Registro {} en tabla {} ya no existe localmente (posiblemente eliminado). Marcando como sincronizado.", reg.getRegistroId(), reg.getTablaNombre());
                    reg.setEstado("SINCRONIZADO");
                    reg.setFechaModificacion(LocalDateTime.now());
                    syncRepository.save(reg);
                    continue;
                }

                // Serializar a mapa plano
                Map<String, Object> flatMap = SyncService.entityToMap(entity);

                // Configurar headers y token de seguridad
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Sync-Token", syncToken);

                PayloadPush payload = new PayloadPush(reg.getRegistroId(), reg.getTablaNombre(), flatMap);
                HttpEntity<PayloadPush> requestEntity = new HttpEntity<>(payload, headers);

                String endpoint = vpsUrl + "/" + reg.getTablaNombre();
                restTemplate.postForEntity(endpoint, requestEntity, Void.class);

                reg.setEstado("SINCRONIZADO");
                reg.setFechaModificacion(LocalDateTime.now());
                syncRepository.save(reg);

                log.info("✅ Registro {} de tabla {} sincronizado exitosamente con el VPS.", reg.getRegistroId(), reg.getTablaNombre());

            } catch (Exception e) {
                log.warn("❌ Falló la sincronización para el registro {} de {}: {}. Se reintentará.", 
                         reg.getRegistroId(), reg.getTablaNombre(), e.getMessage());
                reg.setEstado("ERROR");
                reg.setDetalleError(e.getMessage());
                reg.setFechaModificacion(LocalDateTime.now());
                syncRepository.save(reg);
            }
        }
    }

    @Scheduled(fixedDelay = 300000) // Cada 5 minutos para Pull
    public void procesarPullSincronizacion() {
        if (!habilitada) {
            return;
        }

        try {
            log.info("🔄 Iniciando descarga y sincronización (Pull) del maestro desde el VPS...");

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Sync-Token", syncToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String endpoint = vpsUrl + "/pull";
            ResponseEntity<Map> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, List<Map<String, Object>>> masterData = response.getBody();
                syncService.importMasterData(masterData);
                log.info("✅ Sincronización del maestro (Pull) desde el VPS finalizada con éxito.");
            } else {
                log.warn("⚠️ Recibida respuesta vacía o errónea en pull de sincronización: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("❌ Falló el pull de sincronización desde el VPS: {}", e.getMessage());
        }
    }

    public static class PayloadPush {
        private String id;
        private String tabla;
        private Map<String, Object> data;

        public PayloadPush() {}

        public PayloadPush(String id, String tabla, Map<String, Object> data) {
            this.id = id;
            this.tabla = tabla;
            this.data = data;
        }

        public String getId() { return id; }
        public String getTabla() { return tabla; }
        public Map<String, Object> getData() { return data; }
    }
}
