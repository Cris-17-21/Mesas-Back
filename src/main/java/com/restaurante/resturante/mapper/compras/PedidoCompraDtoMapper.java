package com.restaurante.resturante.mapper.compras;

import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Collections;

import com.restaurante.resturante.domain.compras.PedidoCompra;
import com.restaurante.resturante.domain.compras.DetallePedidoCompra;
import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.compras.TiposPago;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.compras.PedidoCompraDto;
import com.restaurante.resturante.dto.compras.DetallePedidoCompraDto;
import com.restaurante.resturante.domain.inventario.Producto;

@Component
public class PedidoCompraDtoMapper {

    public PedidoCompraDto toDto(PedidoCompra pedido, List<DetallePedidoCompra> detalles) {
        if (pedido == null)
            return null;

        List<DetallePedidoCompraDto> detallesDto = (detalles == null) ? Collections.emptyList()
                : detalles.stream().map(this::toDetalleDto).collect(Collectors.toList());

        return new PedidoCompraDto(
                pedido.getIdPedidoCompra(),
                pedido.getProveedor() != null ? pedido.getProveedor().getIdProveedor() : null,
                pedido.getProveedor() != null ? pedido.getProveedor().getRazonSocial() : null,
                pedido.getUsuario() != null ? pedido.getUsuario().getId() : null,
                pedido.getUsuario() != null ? pedido.getUsuario().getUsername() : null,
                pedido.getFechaPedido(),
                pedido.getFechaEntregaEsperada(),
                pedido.getTipoPago() != null ? pedido.getTipoPago().getIdTipoPago() : null,
                pedido.getTipoPago() != null ? pedido.getTipoPago().getTipoPago() : null,
                pedido.getReferencia(),
                pedido.getObservaciones(),
                pedido.getEstadoPedido(),
                pedido.getTotalPedido(),
                pedido.getAplicaIgv(),
                detallesDto);
    }

    public DetallePedidoCompraDto toDetalleDto(DetallePedidoCompra detalle) {
        if (detalle == null)
            return null;
        return new DetallePedidoCompraDto(
                detalle.getIdDetallePedido(),
                detalle.getProducto() != null ? detalle.getProducto().getIdProducto() : null,
                detalle.getProducto() != null ? detalle.getProducto().getNombreProducto() : null,
                detalle.getCantidadPedida(),
                detalle.getCostoUnitario(),
                detalle.getSubtotalLinea(),
                detalle.getCantidadRecibida());
    }

    public PedidoCompra toEntity(PedidoCompraDto dto, Proveedor proveedor, User usuario, TiposPago tipoPago) {
        if (dto == null)
            return null;

        return PedidoCompra.builder()
                .proveedor(proveedor)
                .usuario(usuario)
                .fechaEntregaEsperada(dto.fechaEntregaEsperada())
                .tipoPago(tipoPago)
                .referencia(dto.referencia())
                .observaciones(dto.observaciones())
                .aplicaIgv(dto.aplicaIgv() != null ? dto.aplicaIgv() : true)
                // Total/Status/Date usually ignored or set by Service logic during creation
                .build();
    }
}
