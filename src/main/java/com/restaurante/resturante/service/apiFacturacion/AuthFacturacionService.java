package com.restaurante.resturante.service.apiFacturacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.restaurante.resturante.dto.apiFacturacion.LoginRequestApiPeru;
import com.restaurante.resturante.dto.apiFacturacion.TokenResponseApiPeru;

@Service
public class AuthFacturacionService {

    private String tokenActual;

    @Value("${api.facturacion.admin.username:}")
    private String adminUsername;

    @Value("${api.facturacion.admin.password:}")
    private String adminPassword;

    @Value("${apisperu.base-url:}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 82800000)
    public void refreshTokenTask() {
        String url = baseUrl + "/auth/login";
        LoginRequestApiPeru loginRequestApiPeru = new LoginRequestApiPeru(adminUsername, adminPassword);

        try {
            TokenResponseApiPeru response = restTemplate.postForObject(url, loginRequestApiPeru,
                    TokenResponseApiPeru.class);
            if (response != null) {
                this.tokenActual = response.token();
                System.out.println("Token actualizado correctamente");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar el token: " + e.getMessage());
        }
    }

    public String getToken() {
        return this.tokenActual;
    }
}
