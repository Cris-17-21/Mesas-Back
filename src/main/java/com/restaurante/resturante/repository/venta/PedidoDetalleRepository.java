package com.restaurante.resturante.repository.venta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.ventas.PedidoDetalle;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, String>{

    // 1. VISUALIZACIÓN: Obtener todos los items de un pedido (para el Ticket o Cocina)
    // Recuerda: pedidoId es String (UUID)
    List<PedidoDetalle> findByPedidoId(String pedidoId);

    // 2. ESTADÍSTICAS: Top de productos más vendidos
    // Devuelve una lista de arrays: [NombreProducto, CantidadTotal]
    // Útil para saber cuál es tu plato estrella ⭐
    @Query("SELECT d.producto.nombreProducto, SUM(d.cantidad) as totalVendido " +
           "FROM PedidoDetalle d " +
           "GROUP BY d.producto.nombreProducto " +
           "ORDER BY totalVendido DESC")
    List<Object[]> encontrarPlatosMasVendidos();

    // 3. SEGURIDAD: Verificar si un producto específico está en uso en algún pedido
    // (Para evitar borrar un producto del menú si ya tiene historial de ventas)
    boolean existsByProductoIdProducto(Integer productoId);

    // 4. MANTENIMIENTO: Borrar todos los detalles de un pedido (si cancelan la orden completa)
    @Modifying
    @Query("DELETE FROM PedidoDetalle d WHERE d.pedido.id = :pedidoId")
    void deleteByPedidoId(String pedidoId);
}
