package com.restaurante.resturante.service.venta.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.mapper.venta.PedidoMapper;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.service.venta.IPedidoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService implements IPedidoService{

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    @Transactional
    public PedidoResponseDto crearPedido(PedidoRequestDto request) {
        // 1. Mapear lo básico a Entidad
        Pedido pedido = Pedido.builder()
                .codigoPedido("PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .tipoEntrega(request.tipoEntrega())
                .estado("PENDIENTE")
                .build();

        // 2. Buscar y asignar Mesa si existe (id es String)
        if (request.mesaId() != null) {
            Mesa mesa = mesaRepository.findById(request.mesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
            pedido.setMesa(mesa);
            mesa.setEstado("OCUPADA"); // Lógica de negocio: ocupar mesa
            mesaRepository.save(mesa);
        }

        // 3. Aquí iría la lógica para buscar Sucursal, Cliente, etc.
        
        Pedido guardado = pedidoRepository.save(pedido);
        return pedidoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDto obtenerPorId(String id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    public List<PedidoResponseDto> listarTodos() {
        return pedidoRepository.findAll().stream()
                .map(pedidoMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDto actualizarEstado(String id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // Lógica de negocio: Si el pedido se cierra, grabamos la fecha de cierre
        if ("ENTREGADO".equalsIgnoreCase(nuevoEstado) || "COMPLETADO".equalsIgnoreCase(nuevoEstado)) {
            pedido.setFechaCierre(java.time.LocalDateTime.now());
            
            // Si tiene mesa, la liberamos automáticamente al entregar/finalizar
            if (pedido.getMesa() != null) {
                Mesa mesa = pedido.getMesa();
                mesa.setEstado("LIBRE"); 
                mesaRepository.save(mesa);
            }
        }

        pedido.setEstado(nuevoEstado.toUpperCase());
        Pedido actualizado = pedidoRepository.save(pedido);
        
        return pedidoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void cancelarPedido(String id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar si se puede cancelar (Ej: no cancelar si ya está ENTREGADO)
        if ("ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new RuntimeException("No se puede cancelar un pedido que ya fue entregado");
        }

        // Liberar la mesa si el pedido se cancela
        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado("LIBRE");
            mesaRepository.save(mesa);
        }

        pedido.setEstado("CANCELADO");
        pedidoRepository.save(pedido);
        
        // Dependiendo de tu política, podrías borrarlo físicamente o solo marcarlo:
        // pedidoRepository.delete(pedido); 
    }
}
