package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.FacturacionComprobante;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository; // Asumiremos que existe o lo creamos

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturacionComprobanteService {

    private final FacturacionComprobanteRepository facturacionRepository;
    private final PedidoRepository pedidoRepository;

    public FacturacionComprobanteDto emitirComprobante(FacturaRequestDto dto) {
        // 1. Validar Pedido
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        if (!"PAGADO".equals(pedido.getEstado()) && !"CERRADO".equals(pedido.getEstado())) {
            // Nota: Podrías permitir facturar antes de pagar, depende del flujo.
            // Por ahora, asumimos que se factura al momento de cobrar o después.
        }

        // 2. Simular Generación de Factura (Aquí iría la llamada a la SUNAT / API
        // externa)
        String serie = "F001";
        String correlativo = String.format("%08d", System.currentTimeMillis() % 1000000);
        String archivoXml = "https://bucket.s3.aws/facturas/" + serie + "-" + correlativo + ".xml";
        String archivoPdf = "https://bucket.s3.aws/facturas/" + serie + "-" + correlativo + ".pdf";

        // 3. Guardar en BD
        FacturacionComprobante comprobante = FacturacionComprobante.builder()
                .pedido(pedido)
                .tipoComprobante(dto.tipoComprobante())
                .serie(serie)
                .correlativo(correlativo)
                .fechaEmision(LocalDateTime.now())
                .rucEmisor("20123456789") // RUC de tu restaurante
                .totalVenta(pedido.getTotalFinal())
                .archivoXml(archivoXml)
                .archivoPdf(archivoPdf)
                .estadoSunat("EMITIDO")
                .build();

        comprobante = facturacionRepository.save(comprobante);

        // 4. Retornar DTO
        return new FacturacionComprobanteDto(
                comprobante.getId(),
                comprobante.getTipoComprobante(),
                comprobante.getSerie(),
                comprobante.getCorrelativo(),
                comprobante.getRucEmisor(),
                comprobante.getFechaEmision().toString(),
                comprobante.getTotalVenta(),
                pedido.getId(),
                comprobante.getArchivoXml(),
                comprobante.getArchivoPdf());
    }
}
