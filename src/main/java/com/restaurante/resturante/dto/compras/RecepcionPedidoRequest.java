package com.restaurante.resturante.dto.compras;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionPedidoRequest {

    private String numeroGuiaRemision;
    private String observaciones;
    private List<DetalleRecepcion> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRecepcion {
        private Long idDetallePedido;
        private Integer cantidadRecibida;
    }
}
