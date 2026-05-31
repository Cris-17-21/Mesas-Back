package com.restaurante.resturante.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import com.restaurante.resturante.service.api_facturacion.FacturacionAuthService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestClientConfig {

    @Value("${api.facturacion.url}")
    private String baseUrl;

    @Bean
    public RestClient facturacionRestClient(ObjectProvider<FacturacionAuthService> authServiceProvider) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    String path = request.getURI().getPath();
                    if (path != null && (path.contains("/auth/login") || path.contains("/auth/refresh"))) {
                        return execution.execute(request, body);
                    }

                    org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);

                    if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.warn("Recibido 401 Unauthorized de la API de facturación. Renovando credenciales y reintentando...");
                        try {
                            String newToken = authServiceProvider.getObject().forceLogin();
                            request.getHeaders().setBearerAuth(newToken);
                            return execution.execute(request, body);
                        } catch (Exception e) {
                            log.error("Fallo al renovar token en el reintento de autenticación: {}", e.getMessage(), e);
                        }
                    }

                    return response;
                })
                .build();
    }
}
