package com.restaurante.resturante.service.api_facturacion;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.domain.api_facturacion.ApiCredencial;
import com.restaurante.resturante.dto.api_facturacion.LoginRequestFacturacion;
import com.restaurante.resturante.dto.api_facturacion.TokenResponseFacturacion;
import com.restaurante.resturante.repository.api_facturacion.ApiCredencialRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FacturacionAuthService {

    private final RestClient restClient;
    private final ApiCredencialRepository repository;

    @Value("${api.facturacion.email}")
    private String email;

    @Value("${api.facturacion.password}")
    private String password;

    public FacturacionAuthService(RestClient restClient, ApiCredencialRepository repository) {
        this.restClient = restClient;
        this.repository = repository;
    }

    public String getValidToken() {
        ApiCredencial session = repository.findById(1L)
                .orElseGet(this::login);

        if (session.getExpiryDate() != null && session.getExpiryDate().isBefore(LocalDateTime.now().plusMinutes(5))) {
            return refresh(session.getRefreshToken());
        }

        return session.getAccessToken();
    }

    public String getApiCompanyId() {
        ApiCredencial session = repository.findById(1L)
                .orElseGet(this::login);
        return session.getApiCompanyId();
    }

    private ApiCredencial login() {
        LoginRequestFacturacion loginRequest = new LoginRequestFacturacion(email, password);

        TokenResponseFacturacion response = restClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .retrieve()
                .body(TokenResponseFacturacion.class);

        return saveSession(response);
    }

    private String refresh(String refreshToken) {
        try {
            TokenResponseFacturacion response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/auth/refresh")
                            .queryParam("refreshToken", refreshToken)
                            .build())
                    .retrieve()
                    .body(TokenResponseFacturacion.class);

            return saveSession(response).getAccessToken();
        } catch (Exception e) {
            return login().getAccessToken();
        }
    }

    private static final UUID NULL_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private ApiCredencial saveSession(TokenResponseFacturacion response) {
        ApiCredencial session = repository.findById(1L).orElse(new ApiCredencial());
        session.setId(1L);
        session.setAccessToken(response.accessToken());
        session.setRefreshToken(response.refreshToken());

        UUID newCompanyId = response.idCompany();
        if (newCompanyId != null && !NULL_COMPANY_ID.equals(newCompanyId)) {
            session.setApiCompanyId(newCompanyId.toString());
        } else if (session.getApiCompanyId() == null) {
            session.setApiCompanyId(newCompanyId != null ? newCompanyId.toString() : null);
        } else {
            log.info("Login devolvió companyId nulo ({}), manteniendo el existente: {}",
                    newCompanyId, session.getApiCompanyId());
        }

        session.setExpiryDate(LocalDateTime.now().plusHours(1));
        return repository.save(session);
    }
}
