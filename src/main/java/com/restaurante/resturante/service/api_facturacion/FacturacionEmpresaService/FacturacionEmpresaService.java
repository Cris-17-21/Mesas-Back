package com.restaurante.resturante.service.api_facturacion.FacturacionEmpresaService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.domain.api_facturacion.ApiCredencial;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.api_facturacion.empresa_facturacion.FacturacionEmpresaRequest;
import com.restaurante.resturante.dto.api_facturacion.empresa_facturacion.FacturacionEmpresaResponse;
import com.restaurante.resturante.repository.api_facturacion.ApiCredencialRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionEmpresaService {
    private final RestClient restClient;
    private final FacturacionAuthService authService;
    private final ApiCredencialRepository credencialRepository;

    public FacturacionEmpresaResponse crearEmpresa(Empresa saved) {
        String token = authService.getValidToken();

        FacturacionEmpresaResponse response = tryCreate(saved, token);

        if (response != null) {
            return response;
        }

        return findAndSaveExisting(saved, token);
    }

    private FacturacionEmpresaResponse tryCreate(Empresa saved, String token) {
        String logoBase64 = saved.getLogoUrl() != null
                ? Base64.getEncoder().encodeToString(saved.getLogoUrl())
                : null;

        FacturacionEmpresaRequest request = new FacturacionEmpresaRequest(
                saved.getRuc(),
                saved.getRazonSocial(),
                saved.getNombreComercial(),
                saved.getDireccionFiscal(),
                saved.getUbigeo(),
                saved.getDepartamento(),
                saved.getProvincia(),
                saved.getDistrito(),
                saved.getUsuarioSol(),
                saved.getClaveSol(),
                null,
                null,
                saved.getCertificadoDigital(),
                logoBase64,
                saved.getEntorno());

        try {
            log.info("Creando empresa en API facturacion: ruc={}", saved.getRuc());

            FacturacionEmpresaResponse response = restClient.post()
                    .uri("/api/v1/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .body(FacturacionEmpresaResponse.class);

            if (response != null && response.id() != null) {
                saveCredentials(response);
                log.info("Empresa creada exitosamente en API, companyId={}", response.id());
                return response;
            }
        } catch (Exception e) {
            log.warn("No se pudo crear la empresa en API (probablemente ya existe): {}", e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private FacturacionEmpresaResponse findAndSaveExisting(Empresa saved, String token) {
        try {
            log.info("Buscando empresa existente en API por RUC: {}", saved.getRuc());

            List<java.util.Map<String, Object>> companies = restClient.get()
                    .uri("/api/v1/companies")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(List.class);

            if (companies != null) {
                for (java.util.Map<String, Object> c : companies) {
                    String ruc = (String) c.get("ruc");
                    if (saved.getRuc().equals(ruc)) {
                        String companyId = c.get("id") != null ? c.get("id").toString() : null;
                        Boolean activo = c.get("activo") != null ? (Boolean) c.get("activo") : false;

                        if (companyId != null && activo) {
                            log.info("Empresa encontrada en API, companyId={}", companyId);
                            saveCompanyId(companyId);
                            return null;
                        }
                    }
                }
            }

            log.warn("No se encontró la empresa con RUC {} en la API", saved.getRuc());
        } catch (Exception e) {
            log.error("Error al buscar empresa en API: {}", e.getMessage());
        }

        return null;
    }

    private void saveCredentials(FacturacionEmpresaResponse response) {
        ApiCredencial session = getOrCreateSession();
        session.setAccessToken(response.accessToken());
        session.setRefreshToken(response.refreshToken());
        session.setApiCompanyId(response.id().toString());
        session.setExpiryDate(LocalDateTime.now().plusHours(1));
        credencialRepository.save(session);
        log.info("Credenciales actualizadas con companyId={}", response.id());
    }

    private void saveCompanyId(String companyId) {
        ApiCredencial session = getOrCreateSession();
        session.setApiCompanyId(companyId);
        credencialRepository.save(session);
        log.info("CompanyId {} almacenado en credenciales", companyId);
    }

    private ApiCredencial getOrCreateSession() {
        return credencialRepository.findById(1L).orElseGet(() -> {
            ApiCredencial s = new ApiCredencial();
            s.setId(1L);
            return s;
        });
    }
}
