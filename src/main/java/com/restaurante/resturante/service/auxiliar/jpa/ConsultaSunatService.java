package com.restaurante.resturante.service.auxiliar.jpa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.dto.auxiliar.EmpresaDto;
import com.restaurante.resturante.dto.auxiliar.PersonaDto;
import com.restaurante.resturante.service.auxiliar.IConsultaSunatService;


@Service
public class ConsultaSunatService implements IConsultaSunatService {

    private final RestClient restClient;

    public ConsultaSunatService(@Value("${token.consulta}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.decolecta.com/v1/")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-type", "applicationjson")
                .build();
    }

    @Override
    public PersonaDto consultarDni(String dni) {
        return restClient.get()
            .uri("/reniec/dni?numero={dni}", dni)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                throw new RuntimeException("Error al consultar DNI: " + res.getStatusCode());
            })
            .body(PersonaDto.class);
    }

    @Override
    public EmpresaDto consultarRuc(String ruc) {
        return restClient.get()
            .uri("/sunat/ruc?numero={ruc}", ruc)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                throw new RuntimeException("Error al consultar RUC: " + res.getStatusCode());
            })
            .body(EmpresaDto.class);
    }
 }
