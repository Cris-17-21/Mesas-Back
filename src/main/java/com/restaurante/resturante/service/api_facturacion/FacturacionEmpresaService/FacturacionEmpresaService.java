package com.restaurante.resturante.service.api_facturacion.FacturacionEmpresaService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.restaurante.resturante.domain.api_facturacion.ApiCredencial;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.api_facturacion.empresa_facturacion.FacturacionEmpresaRequest;
import com.restaurante.resturante.dto.api_facturacion.empresa_facturacion.FacturacionEmpresaResponse;
import com.restaurante.resturante.repository.api_facturacion.ApiCredencialRepository;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
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
    private final EmpresaRepository empresaRepository;

    public FacturacionEmpresaResponse crearEmpresa(Empresa saved) {
        String token = authService.getValidToken();

        FacturacionEmpresaResponse response = tryCreate(saved, token);

        if (response != null) {
            return response;
        }

        return findAndSaveExisting(saved, token);
    }

    public void actualizarEmpresa(Empresa updated) {
        String token = authService.getValidToken();
        String companyId = updated.getApiCompanyId();

        if (companyId == null) {
            log.warn("companyId no encontrado en la empresa local, intentando crear/buscar la empresa en la API primero.");
            crearEmpresa(updated);
            companyId = updated.getApiCompanyId();
            if (companyId == null) {
                log.error("No se pudo obtener el companyId de la API para el RUC {}", updated.getRuc());
                return;
            }
        }

        String logoBase64 = updated.getLogoUrl() != null
                ? Base64.getEncoder().encodeToString(updated.getLogoUrl())
                : null;

        FacturacionEmpresaRequest request = new FacturacionEmpresaRequest(
                null, // RUC no se actualiza (evita errores de unicidad/validación en PATCH)
                null, // Razón Social no se actualiza (evita errores de unicidad/validación en PATCH)
                updated.getNombreComercial(),
                updated.getDireccionFiscal(),
                updated.getUbigeo(),
                updated.getDepartamento(),
                updated.getProvincia(),
                updated.getDistrito(),
                updated.getUsuarioSol(),
                updated.getClaveSol(),
                null,
                null,
                updated.getCertificadoDigital(),
                logoBase64,
                updated.getEntorno());

        try {
            log.info("Actualizando empresa en API facturacion (PATCH): ruc={}, companyId={}", updated.getRuc(), companyId);

            final String finalCompanyId = companyId;
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/companies/me")
                            .queryParam("idCompany", finalCompanyId)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Empresa actualizada exitosamente en API con PATCH, companyId={}", companyId);
        } catch (Exception e) {
            log.error("Error al actualizar empresa en API facturacion: {}", e.getMessage(), e);
        }
    }

    public void actualizarLogo(Empresa empresa, MultipartFile file) {
        String token = authService.getValidToken();
        String companyId = empresa.getApiCompanyId();

        if (companyId == null) {
            log.warn("companyId no encontrado en la empresa local al actualizar logo. Intentando crear la empresa...");
            crearEmpresa(empresa);
            companyId = empresa.getApiCompanyId();
            if (companyId == null) {
                log.warn("companyId no encontrado al actualizar logo, abortando.");
                return;
            }
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("logo", file.getResource());

            log.info("Subiendo logo a la API de facturación para la empresa: {}", empresa.getRuc());

            final String finalCompanyId = companyId;
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/companies/me/logo")
                            .queryParam("idCompany", finalCompanyId)
                            .build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("Authorization", "Bearer " + token)
                    .body(builder.build())
                    .retrieve()
                    .toBodilessEntity();

            log.info("Logo sincronizado exitosamente con la API.");
        } catch (Exception e) {
            log.error("Error al sincronizar el logo con la API: {}", e.getMessage(), e);
        }
    }

    public void actualizarCertificado(Empresa empresa, MultipartFile file, String clave) {
        String token = authService.getValidToken();
        String companyId = empresa.getApiCompanyId();

        if (companyId == null) {
            log.warn("companyId no encontrado en la empresa local al actualizar certificado. Intentando crear la empresa...");
            crearEmpresa(empresa);
            companyId = empresa.getApiCompanyId();
            if (companyId == null) {
                log.warn("companyId no encontrado al actualizar certificado, abortando.");
                return;
            }
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("certificado", file.getResource());
            builder.part("clave", clave);

            log.info("Subiendo certificado a la API de facturación para la empresa: {}", empresa.getRuc());

            final String finalCompanyId = companyId;
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/companies/me/certificado")
                            .queryParam("idCompany", finalCompanyId)
                            .build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("Authorization", "Bearer " + token)
                    .body(builder.build())
                    .retrieve()
                    .toBodilessEntity();

            log.info("Certificado sincronizado exitosamente con la API.");
        } catch (Exception e) {
            log.error("Error al sincronizar el certificado con la API: {}", e.getMessage(), e);
        }
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
                saveSessionTokens(response);
                saved.setApiCompanyId(response.id().toString());
                empresaRepository.save(saved);
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
                            saved.setApiCompanyId(companyId);
                            empresaRepository.save(saved);

                            return new FacturacionEmpresaResponse(
                                    java.util.UUID.fromString(companyId),
                                    ruc,
                                    saved.getRazonSocial(),
                                    saved.getNombreComercial(),
                                    saved.getDireccionFiscal(),
                                    saved.getUbigeo(),
                                    saved.getDepartamento(),
                                    saved.getProvincia(),
                                    saved.getDistrito(),
                                    saved.getLogoUrl() != null ? Base64.getEncoder().encodeToString(saved.getLogoUrl()) : null,
                                    saved.getEntorno(),
                                    "FREE",
                                    true,
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    token,
                                    null
                            );
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

    private void saveSessionTokens(FacturacionEmpresaResponse response) {
        ApiCredencial session = getOrCreateSession();
        session.setAccessToken(response.accessToken());
        session.setRefreshToken(response.refreshToken());
        session.setExpiryDate(LocalDateTime.now().plusYears(100));
        credencialRepository.save(session);
        log.info("Tokens de sesión de facturación actualizados");
    }

    private ApiCredencial getOrCreateSession() {
        return credencialRepository.findById(1L).orElseGet(() -> {
            ApiCredencial s = new ApiCredencial();
            s.setId(1L);
            return s;
        });
    }
}
