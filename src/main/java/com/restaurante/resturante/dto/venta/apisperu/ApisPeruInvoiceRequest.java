package com.restaurante.resturante.dto.venta.apisperu;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApisPeruInvoiceRequest {
    private String ubigeoEmisor;
    private String tipoDoc;
    private String serie;
    private String correlativo;
    private String fechaEmision;
    private String tipoMoneda;
    private Client client;
    private Company company;
    private BigDecimal mtoOperGravadas;
    private BigDecimal mtoIGV;
    private BigDecimal mtoImpVenta;
    private List<SaleDetail> details;
    private List<Legend> legends;

    @Data
    @Builder
    public static class Client {
        private String tipoDoc;
        private String numDoc;
        private String rznSocial;
        private String address;
    }

    @Data
    @Builder
    public static class Company {
        private String ruc;
        private String razonSocial;
        private String nombreComercial;
        private String address;
    }

    @Data
    @Builder
    public static class SaleDetail {
        private String codProducto;
        private String unidad;
        private BigDecimal cantidad;
        private String descripcion;
        private BigDecimal mtoValorUnitario;
        private BigDecimal mtoIgvItem;
        private BigDecimal mtoValorVenta;
        private BigDecimal mtoPrecioUnitario;
        private String tipAfeIgv;
        private BigDecimal porceIgv;
        private BigDecimal mtoBaseIgv;
    }

    @Data
    @Builder
    public static class Legend {
        private String code;
        private String value;
    }
}
