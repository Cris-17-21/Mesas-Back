package com.restaurante.resturante.controller.sincronizacion;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.service.sincronizacion.SyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final SyncService syncService;

    @Value("${sincronizacion.token:default-secret-sync-token}")
    private String syncToken;

    @PostMapping("/{tableName}")
    public ResponseEntity<?> receivePush(
            @PathVariable String tableName,
            @RequestBody PayloadPush payload,
            @RequestHeader(value = "X-Sync-Token", required = false) String token) {
        
        if (syncToken != null && !syncToken.isEmpty() && !syncToken.equals(token)) {
            log.warn("⚠️ Intento de sincronización push no autorizado en tabla: {}", tableName);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de sincronización inválido");
        }

        try {
            log.info("📥 Recibido push para la tabla: {}, registro ID: {}", tableName, payload.getId());
            syncService.saveFlatMap(tableName, payload.getData());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Falló el procesamiento del push para la tabla {}: {}", tableName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/pull")
    public ResponseEntity<?> servePull(
            @RequestHeader(value = "X-Sync-Token", required = false) String token) {
        
        if (syncToken != null && !syncToken.isEmpty() && !syncToken.equals(token)) {
            log.warn("⚠️ Intento de sincronización pull no autorizado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de sincronización inválido");
        }

        try {
            log.info("📤 Generando pull de datos maestros para sincronización...");
            Map<String, List<Map<String, Object>>> data = syncService.exportMasterData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("❌ Falló la generación del pull de datos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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
        public void setId(String id) { this.id = id; }

        public String getTabla() { return tabla; }
        public void setTabla(String tabla) { this.tabla = tabla; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}
