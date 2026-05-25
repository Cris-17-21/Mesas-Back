package com.restaurante.resturante.service.api_facturacion;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionSerieService {

    private final RestClient restClient;
    private final FacturacionAuthService authService;

    public void crearSeriesPorDefecto(String apiSucursalId) {
        String token = authService.getValidToken();
        String companyId = authService.getApiCompanyId();

        if (companyId == null) {
            log.warn("No hay companyId, no se pueden crear series");
            return;
        }

        crearSerieSiNoExiste("BOLETA", "B001", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("FACTURA", "F001", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("NOTA_CREDITO", "FC01", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("NOTA_CREDITO", "BC01", apiSucursalId, companyId, token);
    }

    private void crearSerieSiNoExiste(String tipoDoc, String serie, String sucursalId, String companyId, String token) {
        if (serieYaExiste(serie, tipoDoc, sucursalId, companyId, token)) {
            log.info("Serie {} ya existe en API para sucursal {}, omitiendo creación", serie, sucursalId);
            return;
        }

        try {
            log.info("Creando serie en API: tipoDoc={}, serie={}", tipoDoc, serie);

            Map<String, Object> request = new java.util.HashMap<>();
            request.put("tipoDoc", tipoDoc);
            request.put("serie", serie);
            request.put("proximoCorrelativo", 1);
            if (sucursalId != null) {
                request.put("idSucursal", sucursalId);
            }

            Map<String, Object> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/series/inicio-correlativo")
                            .queryParam("idCompany", companyId)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("id") != null) {
                log.info("Serie creada en API: id={}, serie={}", response.get("id"), serie);
            }
        } catch (Exception e) {
            log.warn("No se pudo crear serie {} en API: {}", serie, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private boolean serieYaExiste(String serie, String tipoDoc, String sucursalId, String companyId, String token) {
        try {
            List<Map<String, Object>> series = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/series")
                            .queryParam("idCompany", companyId)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(List.class);

            if (series != null) {
                for (Map<String, Object> s : series) {
                    String codSerie = (String) s.get("serie");
                    String codTipoDoc = (String) s.get("tipoDocCodigo");
                    String sid = s.get("sucursalId") != null ? s.get("sucursalId").toString() : null;
                    Boolean activo = s.get("activo") != null ? (Boolean) s.get("activo") : false;

                    String tipoDocCodigo = switch (tipoDoc.toUpperCase()) {
                        case "FACTURA" -> "01";
                        case "BOLETA" -> "03";
                        case "NOTA_CREDITO" -> "07";
                        case "NOTA_DEBITO" -> "08";
                        default -> null;
                    };

                    if (serie.equals(codSerie) && tipoDocCodigo != null && tipoDocCodigo.equals(codTipoDoc) && activo) {
                        // Si la serie ya existe globalmente pero necesitamos una por sucursal,
                        // y la sucursal no tiene una propia, créala
                        if (sucursalId != null && sid == null) {
                            continue;
                        }
                        // Si la sucursal tiene su propia serie, ok
                        if (sid == null || sid.equals(sucursalId)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error verificando si serie existe: {}", e.getMessage());
        }

        return false;
    }
}
