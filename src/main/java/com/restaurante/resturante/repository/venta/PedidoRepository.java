package com.restaurante.resturante.repository.venta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.ventas.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    // Buscar pedido por código corto (ej: "A-123")
    Optional<Pedido> findByCodigoPedido(String codigo);

    // Listar pedidos activos en una sucursal (Cocina / Mozos)
    // Usamos String sucursalId por consistencia con tus UUIDs
    List<Pedido> findBySucursalIdAndEstado(String sucursalId, String estado);

    // Listar todos los pedidos de un turno de caja específico
    List<Pedido> findByCajaTurnoId(String cajaTurnoId);

    // Verificar si una mesa tiene un pedido abierto (Evitar duplicar mesas)
    boolean existsByMesaIdAndEstado(String mesaId, String estado);

    // Calcular total de ventas por método de pago en una caja específica
    @Query("SELECT SUM(pp.monto) FROM PedidoPago pp WHERE pp.cajaTurno.id = :cajaId AND pp.medioPago.nombre = :metodo")
    java.math.BigDecimal sumTotalByCajaAndMetodo(String cajaId, String metodo);
}
