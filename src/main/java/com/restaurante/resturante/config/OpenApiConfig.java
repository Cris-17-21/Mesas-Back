package com.restaurante.resturante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // --- INFORMACIÓN GENERAL DE LA API ---
        Info apiInfo = new Info()
                .title("API TEST")
                .version("1.0.0")
                .description("Esta es la documentación de la API para el Sistema de Restaurantes. " +
                             "Aquí encontrarás todos los endpoints disponibles para la gestión de usuarios, " +
                             "mesas, productos y más.")
                .contact(new Contact()
                        .name("ACM Ingenieros")
                        // .email("soporte@yachaysolutions.com")
                        // .url("https://yachaysolutions.com")
                    )
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        final String securitySchemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Pega tu Access Token JWT aquí. Ejemplo: 'eyJhbGciOi...'");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        return new OpenAPI()
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
    }
}
