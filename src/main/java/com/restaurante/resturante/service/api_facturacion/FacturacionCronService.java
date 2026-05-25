package com.restaurante.resturante.service.api_facturacion;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.restaurante.resturante.service.venta.jpa.FacturacionComprobanteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionCronService {

    private final FacturacionComprobanteService comprobanteService;

    // Se ejecuta cada 2 minutos para procesar los comprobantes pendientes de envío
    @Scheduled(cron = "0 */2 * * * *")
    public void ejecutarEnvioDiferido() {
        log.info("Iniciando tarea programada de envío diferido a SUNAT...");
        try {
            comprobanteService.procesarEnviosDiferidos();
        } catch (Exception e) {
            log.error("Error durante el envío diferido a SUNAT: {}", e.getMessage(), e);
        }
    }
}
