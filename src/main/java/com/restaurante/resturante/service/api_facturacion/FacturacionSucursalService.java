package com.restaurante.resturante.service.api_facturacion;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.api_facturacion.SucursalFacturacionRequest;
import com.restaurante.resturante.repository.maestro.SucursalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionSucursalService {

    private final RestClient restClient;
    private final FacturacionAuthService authService;
    private final SucursalRepository sucursalRepository;
    private final FacturacionSerieService serieService;

    public String syncSucursal(Sucursal sucursal) {
        String token = authService.getValidToken();
        String companyId = authService.getApiCompanyId();

        if (companyId == null) {
            log.warn("No hay companyId, no se puede sincronizar sucursal");
            return null;
        }

        String apiSucursalId = tryCreate(sucursal, companyId, token);
        if (apiSucursalId != null) {
            return apiSucursalId;
        }

        return findByNameAndSave(sucursal, companyId, token);
    }

    private String tryCreate(Sucursal sucursal, String companyId, String token) {
        SucursalFacturacionRequest request = new SucursalFacturacionRequest(
                sucursal.getNombre(),
                sucursal.getDireccion(),
                null, null, null, null,
                sucursal.getTelefono(),
                null);

        try {
            log.info("Creando sucursal en API: nombre={}", sucursal.getNombre());

            Map<String, Object> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/sucursales")
                            .queryParam("idCompany", companyId)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("id") != null) {
                String id = response.get("id").toString();
                log.info("Sucursal creada en API: id={}", id);
                saveApiSucursalId(sucursal, id);
                serieService.crearSeriesPorDefecto(id);
                return id;
            }
        } catch (Exception e) {
            log.warn("No se pudo crear sucursal en API (probablemente ya existe): {}", e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private String findByNameAndSave(Sucursal sucursal, String companyId, String token) {
        try {
            log.info("Buscando sucursal por nombre en API: {}", sucursal.getNombre());

            List<Map<String, Object>> sucursales = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/sucursales")
                            .queryParam("idCompany", companyId)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(List.class);

            if (sucursales != null) {
                for (Map<String, Object> s : sucursales) {
                    String nombre = (String) s.get("nombre");
                    if (sucursal.getNombre().equalsIgnoreCase(nombre)) {
                        String id = s.get("id") != null ? s.get("id").toString() : null;
                        Boolean activo = s.get("activo") != null ? (Boolean) s.get("activo") : false;
                        if (id != null && activo) {
                            log.info("Sucursal encontrada en API: id={}", id);
                            saveApiSucursalId(sucursal, id);
                            serieService.crearSeriesPorDefecto(id);
                            return id;
                        }
                    }
                }
            }

            log.warn("No se encontró sucursal con nombre {} en API", sucursal.getNombre());
        } catch (Exception e) {
            log.error("Error buscando sucursal en API: {}", e.getMessage());
        }

        return null;
    }

    private void saveApiSucursalId(Sucursal sucursal, String apiSucursalId) {
        sucursal.setApiSucursalId(apiSucursalId);
        sucursalRepository.save(sucursal);
        log.info("apiSucursalId={} guardado en sucursal local {}", apiSucursalId, sucursal.getId());
    }

    public String getApiSucursalId(Sucursal sucursal) {
        if (sucursal.getApiSucursalId() != null) {
            return sucursal.getApiSucursalId();
        }
        return syncSucursal(sucursal);
    }
}
