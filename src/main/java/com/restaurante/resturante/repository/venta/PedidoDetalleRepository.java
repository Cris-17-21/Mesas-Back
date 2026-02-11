package com.restaurante.resturante.repository.venta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.ventas.PedidoDetalle;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, String>{

    // Obtener todo lo que se ha pedido en una mesa específica
    List<PedidoDetalle> findByPedidoId(String pedidoId);

    // Consultar items por estado de preparación (para el monitor de cocina)
    List<PedidoDetalle> findByEstadoPreparacion(String estado);
}
