package com.restaurante.resturante.mapper.venta;

import java.util.List;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.venta.PedidoDetalleResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;

@Component
public class PedidoMapper {
    public PedidoResponseDto toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;

        List<PedidoDetalleResponseDto> detalles = pedido.getPedidoDetalles().stream()
                .map(this::toDetalleResponseDTO)
                .toList();

        return new PedidoResponseDto(
            pedido.getId(),
            pedido.getCodigoPedido(),
            pedido.getEstado(),
            pedido.getTipoEntrega(),
            pedido.getTotalFinal(),
            pedido.getFechaCreacion(),
            pedido.getCliente() != null ? pedido.getCliente().getNombreRazonSocial() : "CLIENTE GENERAL",
            pedido.getMesa() != null ? pedido.getMesa().getCodigoMesa() : "DELIVERY",
            detalles
        );
    }

    public PedidoDetalleResponseDto toDetalleResponseDTO(PedidoDetalle detalle) {
        return new PedidoDetalleResponseDto(
            detalle.getId(),
            detalle.getProducto().getNombreProducto(),
            detalle.getCantidad(),
            detalle.getPrecioUnitario(),
            detalle.getTotalLinea(),
            detalle.getEstadoPreparacion(),
            detalle.getObservaciones()
        );
    }
}
