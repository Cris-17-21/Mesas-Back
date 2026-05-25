package com.restaurante.resturante.service.api_facturacion;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.dto.api_facturacion.ClienteFacturacionRequest;
import com.restaurante.resturante.repository.maestro.ClienteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionClienteService {

    private final RestClient restClient;
    private final FacturacionAuthService authService;
    private final ClienteRepository clienteRepository;

    public String syncCliente(Cliente cliente) {
        String token = authService.getValidToken();
        String companyId = authService.getApiCompanyId();

        if (companyId == null) {
            log.warn("No hay companyId, no se puede sincronizar cliente");
            return null;
        }

        String apiClienteId = tryCreate(cliente, companyId, token);
        if (apiClienteId != null) {
            return apiClienteId;
        }

        return findByDocumentAndSave(cliente, companyId, token);
    }

    private String tryCreate(Cliente cliente, String companyId, String token) {
        String tipoDoc = mapTipoDoc(cliente.getTipoDocumento() != null
                ? cliente.getTipoDocumento().getName()
                : null);

        ClienteFacturacionRequest request = new ClienteFacturacionRequest(
                tipoDoc,
                cliente.getNumeroDocumento(),
                cliente.getNombreRazonSocial(),
                cliente.getDireccion(),
                cliente.getCorreo(),
                cliente.getTelefono());

        try {
            log.info("Creando cliente en API: documento={}", cliente.getNumeroDocumento());

            Map<String, Object> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/clientes")
                            .queryParam("idCompany", companyId)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("id") != null) {
                String id = response.get("id").toString();
                log.info("Cliente creado en API: id={}", id);
                saveApiClienteId(cliente, id);
                return id;
            }
        } catch (Exception e) {
            log.warn("No se pudo crear cliente en API (probablemente ya existe): {}", e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private String findByDocumentAndSave(Cliente cliente, String companyId, String token) {
        try {
            log.info("Buscando cliente por documento en API: {}", cliente.getNumeroDocumento());

            String tipoDoc = mapTipoDoc(cliente.getTipoDocumento() != null
                    ? cliente.getTipoDocumento().getName()
                    : null);

            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/clientes")
                            .queryParam("idCompany", companyId)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("content") instanceof List<?> content) {
                for (Object item : content) {
                    if (item instanceof Map<?, ?> c) {
                        String id = (String) c.get("id");
                        String tDoc = (String) c.get("tipoDoc");
                        String nDoc = (String) c.get("numDoc");
                        if (tipoDoc.equals(tDoc) && cliente.getNumeroDocumento().equals(nDoc)) {
                            Boolean activo = c.get("activo") != null ? (Boolean) c.get("activo") : false;
                            if (id != null && activo) {
                                log.info("Cliente encontrado en API: id={}", id);
                                saveApiClienteId(cliente, id);
                                return id;
                            }
                        }
                    }
                }
            }

            log.warn("No se encontró cliente con documento {} en API", cliente.getNumeroDocumento());
        } catch (Exception e) {
            log.error("Error buscando cliente en API: {}", e.getMessage());
        }

        return null;
    }

    private void saveApiClienteId(Cliente cliente, String apiClienteId) {
        cliente.setApiClienteId(apiClienteId);
        clienteRepository.save(cliente);
        log.info("apiClienteId={} guardado en cliente local {}", apiClienteId, cliente.getId());
    }

    public String getApiClienteId(Cliente cliente) {
        if (cliente.getApiClienteId() != null) {
            return cliente.getApiClienteId();
        }
        return syncCliente(cliente);
    }

    private String mapTipoDoc(String name) {
        if (name == null)
            return "1";
        return switch (name.toUpperCase()) {
            case "DNI" -> "1";
            case "CE", "CARNET DE EXTRANJERIA" -> "4";
            case "RUC" -> "6";
            case "PASAPORTE" -> "7";
            default -> "1";
        };
    }
}
