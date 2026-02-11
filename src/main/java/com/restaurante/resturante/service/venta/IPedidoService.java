package com.restaurante.resturante.service.venta;

import java.util.List;

import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;

public interface IPedidoService {

    // Crear un pedido (Mesa, Delivery o Para llevar)
    PedidoResponseDto crearPedido(PedidoRequestDto request);
    
    // Obtener un pedido por su UUID (String)
    PedidoResponseDto obtenerPorId(String id);
    
    // Listar todos los pedidos (útil para el monitor de pedidos)
    List<PedidoResponseDto> listarTodos();
    
    // Cambiar el estado (EJ: PENDIENTE -> PREPARANDO -> ENTREGADO)
    PedidoResponseDto actualizarEstado(String id, String nuevoEstado);
    
    // Cancelar pedido
    void cancelarPedido(String id);
}
