package com.restaurante.resturante.service.api_facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionDetalle;
import com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionRequest;
import com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionResponse;
import com.restaurante.resturante.service.api_facturacion.FacturacionEmpresaService.FacturacionEmpresaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.restaurante.resturante.domain.maestros.Sucursal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturacionApiComprobanteService {

    private final RestClient restClient;
    private final FacturacionAuthService authService;
    private final FacturacionEmpresaService empresaService;
    private final FacturacionSucursalService sucursalService;
    private final FacturacionClienteService clienteApiService;
    private final FacturacionSerieService serieApiService;

    public ComprobanteFacturacionResponse emitir(Pedido pedido, String tipoDoc) {
        return emitir(pedido, tipoDoc, null, null, null, null, null);
    }

    public ComprobanteFacturacionResponse emitir(Pedido pedido, String tipoDoc, LocalDateTime fechaEmision) {
        return emitir(pedido, tipoDoc, null, null, null, null, fechaEmision);
    }

    public ComprobanteFacturacionResponse emitirNotaCredito(Pedido pedido, String tipoDocAfectado,
            String numDocAfectado, String codMotivo, String desMotivo) {
        return emitir(pedido, "07", tipoDocAfectado, numDocAfectado, codMotivo, desMotivo, null);
    }

    private ComprobanteFacturacionResponse emitir(Pedido pedido, String tipoDoc,
            String tipDocAfectado, String numDocAfectado, String codMotivo, String desMotivo,
            LocalDateTime fechaEmision) {

        Sucursal sucursal = pedido.getSucursal();
        ensureCompanyExists(sucursal.getEmpresa());

        String token = authService.getValidToken();
        String companyId = authService.getApiCompanyId();

        if (companyId == null) {
            throw new RuntimeException("La empresa no está sincronizada con el servicio de facturación. " +
                    "Sincronícela primero desde la configuración de la empresa.");
        }

        String idSucursal = sucursalService.getApiSucursalId(sucursal);
        serieApiService.crearSeriesPorDefecto(idSucursal);
        String idCliente = getOrCreateCliente(pedido.getCliente(), companyId, token);

        String codTipoDoc = switch (tipoDoc.toUpperCase()) {
            case "FACTURA", "01" -> "01";
            case "BOLETA", "03" -> "03";
            case "NOTA_CREDITO", "07" -> "07";
            case "NOTA_DEBITO", "08" -> "08";
            default -> throw new IllegalArgumentException("Tipo de documento no soportado: " + tipoDoc);
        };
        String serie = switch (codTipoDoc) {
            case "01" -> "F001";
            case "07" -> {
                String afec = tipDocAfectado != null ? tipDocAfectado : "03";
                yield afec.equals("01") ? "FC01" : "BC01";
            }
            case "08" -> "F001";
            default -> "B001";
        };

        List<ComprobanteFacturacionDetalle> detalles = buildDetalles(pedido.getPedidoDetalles());

        ComprobanteFacturacionRequest request = new ComprobanteFacturacionRequest(
                codTipoDoc,
                serie,
                idCliente,
                idSucursal,
                "0101",
                (fechaEmision != null ? fechaEmision : LocalDateTime.now()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                null,
                "PEN",
                true,
                detalles,
                tipDocAfectado,
                numDocAfectado,
                codMotivo,
                desMotivo);

        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/comprobantes")
                        .queryParam("idCompany", companyId)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .body(ComprobanteFacturacionResponse.class);
    }

    private void ensureCompanyExists(Empresa empresa) {
        String companyId = authService.getApiCompanyId();
        if (companyId == null) {
            log.info("Sincronizando empresa {} con API facturacion...", empresa.getRuc());
            var response = empresaService.crearEmpresa(empresa);
            if (response != null && response.id() != null) {
                log.info("Empresa sincronizada exitosamente, id={}", response.id());
            } else {
                throw new RuntimeException("No se pudo sincronizar la empresa con el servicio de facturación");
            }
        }
    }

    private String getOrCreateCliente(Cliente cliente, String companyId, String token) {
        if (cliente == null) {
            return null;
        }
        return clienteApiService.getApiClienteId(cliente);
    }

    private List<ComprobanteFacturacionDetalle> buildDetalles(List<PedidoDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new RuntimeException("El pedido no tiene detalles");
        }

        return detalles.stream()
                .map(d -> {
                    BigDecimal cantidad = BigDecimal.valueOf(d.getCantidad());
                    BigDecimal precioVenta = d.getPrecioUnitario();
                    BigDecimal valorUnitario = precioVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
                    BigDecimal valorVenta = valorUnitario.multiply(cantidad);
                    BigDecimal igv = valorVenta.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);

                    String codigo = d.getProducto() != null
                            ? String.valueOf(d.getProducto().getIdProducto())
                            : "0";
                    String descripcion = d.getProducto() != null
                            ? d.getProducto().getNombreProducto()
                            : "Producto";
                    String unidad = d.getProducto() != null && d.getProducto().getUnidadMedida() != null
                            ? d.getProducto().getUnidadMedida()
                            : "NIU";

                    return new ComprobanteFacturacionDetalle(
                            codigo,
                            descripcion,
                            unidad,
                            cantidad,
                            valorUnitario,
                            precioVenta,
                            "10",
                            new BigDecimal("18.00"),
                            igv,
                            valorVenta,
                            valorVenta);
                })
                .toList();
    }

    private String mapTipoDoc(String name) {
        if (name == null)
            return "1";
        return switch (name.toUpperCase()) {
            case "DNI" -> "1";
            case "CE", "CARNET DE EXTRANJERIA" -> "4";
            case "RUC" -> "6";
            case "PASAPORTE" -> "7";
            default -> "1";
        };
    }
}
