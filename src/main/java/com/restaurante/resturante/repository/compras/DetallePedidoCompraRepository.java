package com.restaurante.resturante.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.DetallePedidoCompra;

import java.util.List;

@Repository
public interface DetallePedidoCompraRepository extends JpaRepository<DetallePedidoCompra, Long> {
    List<DetallePedidoCompra> findByPedidoCompra_IdPedidoCompra(Long idPedidoCompra);
}
