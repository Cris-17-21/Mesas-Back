package com.restaurante.resturante.service.compras;

import java.util.List;
import java.util.Optional;

import com.restaurante.resturante.dto.compras.PedidoCompraDto;

public interface IPedidoCompraService {
    List<PedidoCompraDto> findAll();

    Optional<PedidoCompraDto> findById(Long id);

    PedidoCompraDto registrarPedido(PedidoCompraDto dto);

    PedidoCompraDto actualizarEstado(Long id, String nuevoEstado);

    PedidoCompraDto registrarRecepcion(Long id, com.restaurante.resturante.dto.compras.RecepcionPedidoRequest request);

    void anularPedido(Long id);
}
