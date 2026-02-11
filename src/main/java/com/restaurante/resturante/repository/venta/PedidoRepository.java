package com.restaurante.resturante.repository.venta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.ventas.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, String>{

    // Buscar el pedido activo de una mesa (que no esté Pagado ni Anulado)
    @Query("SELECT p FROM Pedido p WHERE p.mesa.id = :mesaId AND p.estado NOT IN ('Pagado', 'Anulado')")
    Optional<Pedido> findPedidoActivoPorMesa(String mesaId);

    // Listar todos los pedidos pendientes de una sucursal
    List<Pedido> findBySucursalIdAndEstado(String sucursalId, String estado);

    // Buscar por el código único (ej: PED-102)
    Optional<Pedido> findByCodigoPedido(String codigoPedido);
}
