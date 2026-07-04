package com.restaurante.resturante.repository.venta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") String id);

    // Buscar pedido por código corto (ej: "A-123")
    Optional<Pedido> findByCodigoPedido(String codigo);

    // Listar pedidos activos en una sucursal (Cocina / Mozos)
    // Usamos String sucursalId por consistencia con tus UUIDs
    List<Pedido> findBySucursalIdAndEstado(String sucursalId, String estado);

    // Listar todos los pedidos de un turno de caja específico
    List<Pedido> findByCajaTurnoId(String cajaTurnoId);

    List<Pedido> findBySucursalIdAndTipoEntregaAndEstado(String sucursalId, String tipoEntrega, String estado);

    // Verificar si una mesa tiene un pedido abierto (Evitar duplicar mesas)
    boolean existsByMesaIdAndEstado(String mesaId, String estado);

    // Buscar un detalle de pedido por su ID
    @Query("SELECT pd FROM PedidoDetalle pd WHERE pd.id = :id")
    Optional<PedidoDetalle> findDetalleById(@Param("id") String id);

    // Buscar pedidos por sucursal y estado de preparación de sus detalles
    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.pedidoDetalles pd WHERE p.sucursal.id = :sucursalId AND p.estado = 'ABIERTO' AND pd.estadoPreparacion = :estadoPreparacion")
    List<Pedido> findBySucursalIdAndDetallesEstadoPreparacion(String sucursalId, String estadoPreparacion);

    // Calcular total de ventas por si es efectivo o no en una caja específica
    @Query("SELECT SUM(pp.monto) FROM PedidoPago pp WHERE pp.cajaTurno.id = :cajaId AND pp.medioPago.esEfectivo = :esEfectivo")
    java.math.BigDecimal sumTotalByCajaAndEsEfectivo(String cajaId, boolean esEfectivo);

    // Si quieres mantener el de por nombre pero mejorado
    @Query("SELECT SUM(pp.monto) FROM PedidoPago pp WHERE pp.cajaTurno.id = :cajaId AND pp.medioPago.nombre = :metodo")
    java.math.BigDecimal sumTotalByCajaAndMetodo(String cajaId, String metodo);
}
