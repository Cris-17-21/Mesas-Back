package com.restaurante.resturante.service.venta.jpa;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.FacturacionComprobante;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.dto.venta.apisperu.ApisPeruDocumentResponse;
import com.restaurante.resturante.dto.venta.apisperu.ApisPeruInvoiceRequest;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturacionComprobanteService {

        private final FacturacionComprobanteRepository facturacionRepository;
        private final PedidoRepository pedidoRepository;
        private final ApisPeruService apisPeruService;

        @Transactional
        public FacturacionComprobanteDto emitirComprobante(FacturaRequestDto dto) {
                // 1. Validar Pedido
                Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                // 2. Preparar Request para APIsPERU
                ApisPeruInvoiceRequest apisPeruRequest = mapToApisPeruRequest(pedido, dto);

                // 3. Llamar a APIsPERU
                ApisPeruDocumentResponse response = apisPeruService.enviarFactura(apisPeruRequest).block();

                if (response == null || !response.isSuccess()) {
                        String errorMsg = response != null ? response.getMessage()
                                        : "Error desconocido al contactar APIsPERU";
                        throw new RuntimeException("Error en Facturación Electrónica: " + errorMsg);
                }

                // 4. Guardar en BD con datos reales
                FacturacionComprobante comprobante = FacturacionComprobante.builder()
                                .pedido(pedido)
                                .cliente(pedido.getCliente())
                                .tipoComprobante(dto.tipoComprobante())
                                .serie(apisPeruRequest.getSerie())
                                .correlativo(apisPeruRequest.getCorrelativo())
                                .fechaEmision(LocalDateTime.now())
                                .rucEmisor(pedido.getSucursal().getEmpresa().getRuc())
                                .totalVenta(pedido.getTotalFinal())
                                .archivoXml(response.getXml())
                                .archivoPdf(response.getPdf())
                                .hash(response.getHash())
                                .estadoSunat(response.getSunatResponse() != null
                                                ? response.getSunatResponse().getDescription()
                                                : "ACEPTADO")
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

        private ApisPeruInvoiceRequest mapToApisPeruRequest(Pedido pedido, FacturaRequestDto dto) {
                Sucursal sucursal = pedido.getSucursal();
                Empresa empresa = sucursal.getEmpresa();
                Cliente cliente = pedido.getCliente();

                if (cliente == null) {
                        throw new RuntimeException("EL PEDIDO DEBE TENER UN CLIENTE ASIGNADO PARA FACTURAR");
                }

                String serie = dto.tipoComprobante().equals("01") ? "F001" : "B001";
                String correlativo = String.valueOf(System.currentTimeMillis() % 100000000);

                return ApisPeruInvoiceRequest.builder()
                                .ubigeoEmisor("150101") // Placeholder: Lima Cercado
                                .tipoDoc(dto.tipoComprobante()) // 01=Factura, 03=Boleta
                                .serie(serie)
                                .correlativo(correlativo)
                                .fechaEmision(java.time.OffsetDateTime.now().toString())
                                .tipoMoneda("PEN")
                                .company(ApisPeruInvoiceRequest.Company.builder()
                                                .ruc(empresa.getRuc())
                                                .razonSocial(empresa.getRazonSocial())
                                                .nombreComercial(empresa.getRazonSocial())
                                                .address(empresa.getDireccionFiscal())
                                                .build())
                                .client(ApisPeruInvoiceRequest.Client.builder()
                                                .tipoDoc(mapTipoDoc(cliente.getTipoDocumento().getName()))
                                                .numDoc(cliente.getNumeroDocumento())
                                                .rznSocial(cliente.getNombreRazonSocial())
                                                .address(cliente.getDireccion())
                                                .build())
                                .mtoOperGravadas(pedido.getTotalFinal().divide(new java.math.BigDecimal("1.18"), 2,
                                                java.math.RoundingMode.HALF_UP))
                                .mtoIGV(pedido.getTotalFinal().subtract(pedido.getTotalFinal().divide(
                                                new java.math.BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP)))
                                .mtoImpVenta(pedido.getTotalFinal())
                                .details(pedido.getPedidoDetalles().stream()
                                                .map(det -> ApisPeruInvoiceRequest.SaleDetail.builder()
                                                                .codProducto(det.getProducto().getIdProducto()
                                                                                .toString())
                                                                .unidad("NIU") // Unidades
                                                                .cantidad(new java.math.BigDecimal(det.getCantidad()))
                                                                .descripcion(det.getProducto().getNombreProducto())
                                                                .mtoValorUnitario(det.getPrecioUnitario().divide(
                                                                                new java.math.BigDecimal("1.18"), 2,
                                                                                java.math.RoundingMode.HALF_UP))
                                                                .mtoPrecioUnitario(det.getPrecioUnitario())
                                                                .mtoIgvItem(det.getTotalLinea().subtract(det
                                                                                .getTotalLinea()
                                                                                .divide(new java.math.BigDecimal(
                                                                                                "1.18"), 2,
                                                                                                java.math.RoundingMode.HALF_UP)))
                                                                .mtoBaseIgv(det.getTotalLinea().divide(
                                                                                new java.math.BigDecimal("1.18"), 2,
                                                                                java.math.RoundingMode.HALF_UP))
                                                                .mtoValorVenta(det.getTotalLinea().divide(
                                                                                new java.math.BigDecimal("1.18"), 2,
                                                                                java.math.RoundingMode.HALF_UP))
                                                                .tipAfeIgv("10") // Gravado - Operación Onerosa
                                                                .porceIgv(new java.math.BigDecimal("18"))
                                                                .build())
                                                .toList())
                                .build();
        }

        private String mapTipoDoc(String name) {
                if (name == null)
                        return "1";
                return switch (name.toUpperCase()) {
                        case "DNI" -> "1";
                        case "RUC" -> "6";
                        case "PASAPORTE" -> "7";
                        default -> "1";
                };
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
        public FacturacionComprobanteDto emitirNotaCredito(
                        com.restaurante.resturante.dto.venta.NotaCreditoRequestDto dto) {
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
