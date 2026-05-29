package com.restaurante.resturante.service.sincronizacion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sincronizacion.habilitada:false}")
    private boolean habilitada;

    @Value("${sincronizacion.vps.url:http://localhost:8080/api/sync}")
    private String vpsUrl;

    @Scheduled(fixedDelay = 30000) // Cada 30 segundos
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
                // Enviar el ID y la tabla, y que el VPS procese
                String endpoint = vpsUrl + "/" + reg.getTablaNombre();
                
                PayloadSync payload = new PayloadSync(reg.getRegistroId(), reg.getTablaNombre());
                
                restTemplate.postForEntity(endpoint, payload, Void.class);

                reg.setEstado("SINCRONIZADO");
                reg.setFechaModificacion(LocalDateTime.now());
                syncRepository.save(reg);

                log.info("✅ Registro {} sincronizado exitosamente.", reg.getRegistroId());

            } catch (Exception e) {
                log.warn("❌ Fallo la sincronizacion para el registro {}: {}. Se reintentara.", 
                         reg.getRegistroId(), e.getMessage());
                reg.setEstado("ERROR");
                reg.setDetalleError(e.getMessage());
                syncRepository.save(reg);
            }
        }
    }

    public static class PayloadSync {
        private String id;
        private String tabla;

        public PayloadSync(String id, String tabla) {
            this.id = id;
            this.tabla = tabla;
        }

        public String getId() { return id; }
        public String getTabla() { return tabla; }
    }
}
