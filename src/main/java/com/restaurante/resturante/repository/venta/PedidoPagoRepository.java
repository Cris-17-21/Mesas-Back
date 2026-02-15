package com.restaurante.resturante.repository.venta;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.domain.ventas.PedidoPago;

public interface PedidoPagoRepository extends JpaRepository<PedidoPago, String> {
    
    // Ver los pagos de un pedido específico (para imprimir en el ticket)
    List<PedidoPago> findByPedidoId(String pedidoId);

    // ¡ESTA ES LA CONSULTA QUE TE FALLABA ANTES! (Corregida)
    // Suma los montos pagados filtrando por el Turno de Caja y el Método de Pago.
    // Navegamos: PedidoPago -> Pedido -> CajaTurno
    @Query("SELECT COALESCE(SUM(pp.monto), 0) " +
           "FROM PedidoPago pp " +
           "WHERE pp.pedido.cajaTurno.id = :cajaTurnoId " +
           "AND pp.medioPago = :metodo")
    BigDecimal sumarPorMetodoYTurno(String cajaTurnoId, MedioPago metodo);
    
}
