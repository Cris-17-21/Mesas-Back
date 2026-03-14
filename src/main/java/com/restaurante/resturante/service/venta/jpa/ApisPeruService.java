package com.restaurante.resturante.service.venta.jpa;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.restaurante.resturante.dto.venta.apisperu.ApisPeruDocumentResponse;
import com.restaurante.resturante.dto.venta.apisperu.ApisPeruInvoiceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApisPeruService {

    private final WebClient apisPeruWebClient;

    public Mono<ApisPeruDocumentResponse> enviarFactura(ApisPeruInvoiceRequest request) {
        log.info("Enviando factura a APIsPERU: {} - {}", request.getSerie(), request.getCorrelativo());

        return apisPeruWebClient.post()
                .uri("/invoice/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ApisPeruDocumentResponse.class)
                .doOnError(error -> log.error("Error al enviar factura a APIsPERU", error));
    }
}
