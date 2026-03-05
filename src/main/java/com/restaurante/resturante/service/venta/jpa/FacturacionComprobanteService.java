package com.restaurante.resturante.service.venta.jpa;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.FacturacionComprobante;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturacionComprobanteService {

    private final FacturacionComprobanteRepository facturacionRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public FacturacionComprobanteDto emitirComprobante(FacturaRequestDto dto) {
        // 1. Validar Pedido
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        // 2. Simular Generación de Factura
        String serie = dto.tipoComprobante().equals("01") ? "F001" : "B001";
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
                .rucEmisor("20123456789")
                .totalVenta(pedido.getTotalFinal())
                .archivoXml(archivoXml)
                .archivoPdf(archivoPdf)
                .estadoSunat("EMITIDO")
                .empresa(pedido.getSucursal().getEmpresa())
                .sucursal(pedido.getSucursal())
                .build();

        comprobante = facturacionRepository.save(comprobante);

        return new FacturacionComprobanteDto(
                comprobante.getId(), comprobante.getTipoComprobante(), comprobante.getSerie(),
                comprobante.getCorrelativo(), comprobante.getRucEmisor(),
                comprobante.getFechaEmision().toString(), comprobante.getTotalVenta(),
                pedido.getId(), comprobante.getArchivoXml(), comprobante.getArchivoPdf());
    }

    @Transactional(readOnly = true)
    public java.util.List<FacturacionComprobanteDto> listarComprobantes(String sucursalId) {
        return facturacionRepository.findBySucursalIdOrderByFechaEmisionDesc(sucursalId).stream()
                .map(c -> new FacturacionComprobanteDto(
                        c.getId(), c.getTipoComprobante(), c.getSerie(), c.getCorrelativo(),
                        c.getRucEmisor(), c.getFechaEmision().toString(), c.getTotalVenta(),
                        c.getPedido() != null ? c.getPedido().getId() : null,
                        c.getArchivoXml(), c.getArchivoPdf()))
                .toList();
    }

    @Transactional
    public FacturacionComprobanteDto emitirNotaCredito(com.restaurante.resturante.dto.venta.NotaCreditoRequestDto dto) {
        FacturacionComprobante ref = facturacionRepository.findById(dto.comprobanteId())
                .orElseThrow(() -> new RuntimeException("COMPROBANTE DE REFERENCIA NO ENCONTRADO"));

        // 1. Crear Nota de Crédito
        FacturacionComprobante nc = FacturacionComprobante.builder()
                .tipoComprobante("07") // 07=Nota de Crédito
                .serie(ref.getSerie().startsWith("F") ? "FC01" : "BC01")
                .correlativo(String.format("%08d", System.currentTimeMillis() % 1000000))
                .fechaEmision(LocalDateTime.now())
                .empresa(ref.getEmpresa())
                .sucursal(ref.getSucursal())
                .pedido(ref.getPedido())
                .cliente(ref.getCliente())
                .totalVenta(ref.getTotalVenta().negate())
                .comprobanteReferencia(ref)
                .codMotivoNota(dto.codMotivo())
                .descripcionMotivo(dto.descripcion())
                .estadoSunat("EMITIDO")
                .rucEmisor(ref.getRucEmisor())
                .build();

        nc = facturacionRepository.save(nc);

        return new FacturacionComprobanteDto(
                nc.getId(), nc.getTipoComprobante(), nc.getSerie(), nc.getCorrelativo(),
                nc.getRucEmisor(), nc.getFechaEmision().toString(), nc.getTotalVenta(),
                nc.getPedido() != null ? nc.getPedido().getId() : null,
                null, null);
    }
}
