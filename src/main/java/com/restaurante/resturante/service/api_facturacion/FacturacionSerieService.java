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

    public void crearSeriesPorDefecto(String apiSucursalId, String companyId) {
        String token = authService.getValidToken();
        if (companyId == null) {
            log.warn("No se puede crear series: empresa no sincronizada");
            return;
        }

        crearSerieSiNoExiste("01", "F001", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("03", "B001", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("07", "FC01", apiSucursalId, companyId, token);
        crearSerieSiNoExiste("07", "BC01", apiSucursalId, companyId, token);

        log.info("Series por defecto creadas/verificadas para sucursal {}", apiSucursalId);
    }

    private void crearSerieSiNoExiste(String tipoDoc, String serie, String sucursalId, String companyId, String token) {
        if (serieYaExiste(serie, tipoDoc, sucursalId, companyId, token)) {
            log.info("Serie {} ya existe en API para sucursal {}, omitiendo creación", serie, sucursalId);
            return;
        }

        try {
            log.info("Creando serie en API: tipoDoc={}, serie={}", tipoDoc, serie);

            String mappedTipoDoc = switch (tipoDoc) {
                case "01", "FACTURA" -> "FACTURA";
                case "03", "BOLETA" -> "BOLETA";
                case "07", "NOTA_CREDITO" -> "NOTA_CREDITO";
                case "08", "NOTA_DEBITO" -> "NOTA_DEBITO";
                case "09", "GUIA_REMISION" -> "GUIA_REMISION";
                default -> tipoDoc.toUpperCase();
            };

            Map<String, Object> request = new java.util.HashMap<>();
            request.put("tipoDoc", mappedTipoDoc);
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
            String path = (sucursalId != null) ? "/api/v1/series/sucursal/" + sucursalId : "/api/v1/series";
            List<Map<String, Object>> series = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
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
                    Boolean activo = s.get("activo") != null ? (Boolean) s.get("activo") : true;

                    String tipoDocCodigo = switch (tipoDoc.toUpperCase()) {
                        case "FACTURA", "01" -> "01";
                        case "BOLETA", "03" -> "03";
                        case "NOTA_CREDITO", "07" -> "07";
                        case "NOTA_DEBITO", "08" -> "08";
                        case "GUIA_REMISION", "09" -> "09";
                        default -> null;
                    };

                    if (serie.equals(codSerie) && tipoDocCodigo != null && tipoDocCodigo.equals(codTipoDoc)
                            && Boolean.TRUE.equals(activo)) {
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
