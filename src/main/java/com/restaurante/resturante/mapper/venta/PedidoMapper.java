package com.restaurante.resturante.mapper.venta;

import java.util.List;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.dto.venta.PedidoDetalleResponseDto;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResumenDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PedidoMapper {

        private final PedidoDetalleMapper detalleMapper;

        public Pedido toEntity(PedidoRequestDto dto) {
                if (dto == null)
                        return null;

                return Pedido.builder()
                                .tipoEntrega(dto.tipoEntrega())
                                .estado("ABIERTO")
                                // Nota: Las relaciones (Mesa, Cliente, Sucursal)
                                // se asignan en el Service buscando en el Repo
                                .build();
        }

        public PedidoResponseDto toDto(Pedido entity) {
                if (entity == null)
                        return null;

                // 1. Lógica para campos calculados o condicionales
                String clienteNombre = (entity.getCliente() != null)
                                ? entity.getCliente().getNombreRazonSocial()
                                : "CLIENTE GENERAL";

                String mesaCodigo = (entity.getMesa() != null)
                                ? entity.getMesa().getCodigoMesa()
                                : "N/A";

                List<PedidoDetalleResponseDto> listaDetalles = (entity.getPedidoDetalles() != null)
                                ? entity.getPedidoDetalles().stream().map(detalleMapper::toDto).toList()
                                : java.util.Collections.emptyList();

                // 2. Constructor de 10 parámetros según tu nuevo Record
                return new PedidoResponseDto(
                                entity.getId(), // 1
                                entity.getCodigoPedido(), // 2
                                entity.getEstado(), // 3
                                entity.getTipoEntrega(), // 4
                                entity.getTotalFinal(), // 5
                                entity.getFechaCreacion(), // 6
                                clienteNombre, // 7
                                mesaCodigo, // 8
                                listaDetalles, // 9
                                entity.getSucursal() != null ? entity.getSucursal().getId() : null // 10. sucursalId
                );
        }

        public PedidoResumenDto toResumenDto(Pedido entity) {
                if (entity == null)
                        return null;

                // 1. Extraemos valores con lógica de nulos
                String clienteNombre = (entity.getCliente() != null)
                                ? entity.getCliente().getNombreRazonSocial()
                                : "CLIENTE GENERAL";

                String mesaCodigo = (entity.getMesa() != null)
                                ? entity.getMesa().getCodigoMesa()
                                : "N/A";

                // 2. Constructor con los 9 parámetros en el orden exacto del Record
                return new PedidoResumenDto(
                                entity.getId(), // 1. id
                                entity.getCodigoPedido(), // 2. codigoPedido
                                entity.getEstado(), // 3. estado
                                entity.getTipoEntrega(), // 4. tipoEntrega
                                entity.getTotalFinal(), // 5. totalFinal
                                entity.getFechaCreacion(), // 6. fechaCreacion
                                clienteNombre, // 7. nombreCliente
                                mesaCodigo, // 8. codigoMesa
                                entity.getMesa() != null ? entity.getMesa().getId() : null // 9. mesaId
                );
        }
}
