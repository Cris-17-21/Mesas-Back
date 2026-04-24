package com.restaurante.resturante.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.PedidoCompra;

import java.util.List;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    List<PedidoCompra> findBySucursal_Id(String sucursalId);
}
