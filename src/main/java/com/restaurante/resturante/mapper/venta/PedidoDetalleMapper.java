package com.restaurante.resturante.mapper.venta;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.venta.PedidoDetalleRequestDto;
import com.restaurante.resturante.dto.venta.PedidoDetalleResponseDto;

@Component
public class PedidoDetalleMapper {

    public PedidoDetalle toEntity(PedidoDetalleRequestDto dto) {
        if (dto == null) return null;

        return PedidoDetalle.builder()
                .cantidad(dto.cantidad())
                .observaciones(dto.observaciones())
                .estadoPreparacion("PENDIENTE")
                .estadoPago("PENDIENTE")
                .cantidadPagada(0)
                .build();
    }

    public PedidoDetalleResponseDto toDto(PedidoDetalle entity) {
        if (entity == null) return null;

        return new PedidoDetalleResponseDto(
                entity.getId(),
                entity.getProducto().getNombreProducto(),
                entity.getCantidad(),
                entity.getCantidadPagada(),
                entity.getPrecioUnitario(),
                entity.getTotalLinea(),
                entity.getEstadoPreparacion(),
                entity.getEstadoPago(),
                entity.getObservaciones()
        );
    }
}
