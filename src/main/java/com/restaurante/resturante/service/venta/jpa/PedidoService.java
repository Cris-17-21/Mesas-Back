package com.restaurante.resturante.service.venta.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.maestro.UnionMesaRequest;
import com.restaurante.resturante.dto.venta.PedidoDetalleRequestDto;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResumenDto;
import com.restaurante.resturante.dto.venta.SepararCuentaDto;
import com.restaurante.resturante.mapper.venta.PedidoDetalleMapper;
import com.restaurante.resturante.mapper.venta.PedidoMapper;
import com.restaurante.resturante.repository.inventario.ProductoRepository;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.service.maestros.IMesaService;
import com.restaurante.resturante.service.venta.IPedidoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final CajaTurnoRepository cajaRepository;
    private final MesaRepository mesaRepository;
    private final ProductoRepository productoRepository;
    private final IMesaService mesaService; // Inyectamos tu service de mesas
    private final PedidoMapper pedidoMapper;
    private final PedidoDetalleMapper detalleMapper;
    private final SucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PedidoResponseDto crearPedido(PedidoRequestDto dto) {
        // 1. Validar Caja Abierta
        var caja = cajaRepository.findByUserIdAndSucursalIdAndEstado(dto.usuarioId(), dto.sucursalId(), "ABIERTA")
                .orElseThrow(() -> new RuntimeException("DEBE ABRIR CAJA PARA REGISTRAR PEDIDOS"));

        // 2. Crear Pedido y asignar código
        Pedido pedido = pedidoMapper.toEntity(dto);
        pedido.setCodigoPedido("PED-" + System.currentTimeMillis() % 10000);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setCajaTurno(caja);

        // **Nuevo paso: cargar y setear sucursal**
        Sucursal sucursal = sucursalRepository.findById(dto.sucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        pedido.setSucursal(sucursal);

        User usuario = userRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        pedido.setUser(usuario);

        // 3. Procesar Detalles
        List<PedidoDetalle> detalles = dto.detalles().stream()
                .map(d -> {
                    PedidoDetalle det = detalleMapper.toEntity(d);
                    det.setPedido(pedido);

                    Integer prodId = Integer.parseInt(d.productoId());
                    Producto producto = productoRepository.findById(prodId)
                            .orElseThrow(() -> new RuntimeException("PRODUCTO NO ENCONTRADO: " + d.productoId()));
                    det.setProducto(producto);
                    det.setPrecioUnitario(producto.getPrecioVenta());
                    det.setTotalLinea(producto.getPrecioVenta().multiply(new java.math.BigDecimal(det.getCantidad())));
                    return det;
                }).toList();

        pedido.setPedidoDetalles(detalles);
        pedido.calcularTotales();

        // 4. Cambiar estado de mesa y asociarla
        if (dto.mesaId() != null && !dto.mesaId().isBlank()) {
            Mesa mesa = mesaRepository.findById(dto.mesaId())
                    .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + dto.mesaId()));
            pedido.setMesa(mesa);
            mesaService.cambiarEstado(dto.mesaId(), "OCUPADA");
        }

        return pedidoMapper.toDto(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoResponseDto actualizarDetalles(String pedidoId, List<PedidoDetalleRequestDto> nuevosDetalles) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        if (!"ABIERTO".equals(pedido.getEstado())) {
            throw new RuntimeException("EL PEDIDO YA NO ESTÁ ABIERTO");
        }

        List<PedidoDetalle> extras = nuevosDetalles.stream()
                .map(d -> {
                    PedidoDetalle det = detalleMapper.toEntity(d);
                    det.setPedido(pedido);
                    return det;
                }).toList();

        pedido.getPedidoDetalles().addAll(extras);
        pedido.calcularTotales(); // Recalcula total_final

        return pedidoMapper.toDto(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public void unirMesas(UnionMesaRequest dto) {
        // Delegamos la lógica física al MesaService
        mesaService.unirMesas(dto.idPrincipal(), dto.idsSecundarios());
        // Aquí podrías añadir lógica extra si el pedido debe mostrar las mesas unidas
    }

    @Override
    @Transactional
    public void registrarPago(String pedidoId, String metodoPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        // 1. Cerrar Pedido
        pedido.setEstado("PAGADO");
        pedido.setFechaCierre(LocalDateTime.now());
        // pedido.setMetodoPago(metodoPago.toUpperCase());

        // 2. Liberar Mesas (Principal y unidas)
        String mesaId = pedido.getMesa().getId();
        mesaService.separarMesas(mesaId); // Libera secundarias
        mesaService.cambiarEstado(mesaId, "LIBRE"); // Libera principal

        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDto obtenerPorId(String id) {
        return pedidoRepository.findById(id)
                .map(pedidoMapper::toDto)
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenDto> listarPedidosActivos(String sucursalId) {
        // Nota: Asegúrate de tener este método en el repository
        return pedidoRepository.findBySucursalIdAndEstado(sucursalId, "ABIERTO")
                .stream()
                .map(pedidoMapper::toResumenDto)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDto separarCuenta(SepararCuentaDto dto) {
        // 1. Obtener Pedido Origen
        Pedido origen = pedidoRepository.findById(dto.pedidoOrigenId())
                .orElseThrow(() -> new RuntimeException("PEDIDO ORIGEN NO ENCONTRADO"));

        if (!"ABIERTO".equals(origen.getEstado())) {
            throw new RuntimeException("EL PEDIDO ORIGEN DEBE ESTAR ABIERTO");
        }

        // 3. Crear Nuevo Pedido (Clonando datos clave)
        Pedido nuevo = new Pedido();
        nuevo.setCodigoPedido("PED-SEP-" + System.currentTimeMillis() % 10000);
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setEstado("ABIERTO");
        nuevo.setTipoEntrega(origen.getTipoEntrega());
        nuevo.setSucursal(origen.getSucursal());
        nuevo.setCajaTurno(origen.getCajaTurno());
        nuevo.setUser(origen.getUser());

        // Si no se especifica nueva mesa, se queda en la misma (cuenta dividida en
        // mesa)
        if (dto.nuevaMesaId() != null && !dto.nuevaMesaId().isBlank()) {
            Mesa nuevaMesa = mesaRepository.findById(dto.nuevaMesaId())
                    .orElseThrow(() -> new RuntimeException("MESA DESTINO NO ENCONTRADA"));
            nuevo.setMesa(nuevaMesa);
            mesaService.cambiarEstado(nuevaMesa.getId(), "OCUPADA");
        } else {
            nuevo.setMesa(origen.getMesa());
        }

        // 2. Procesar items a mover
        List<PedidoDetalle> nuevosDetalles = new java.util.ArrayList<>();

        for (var itemDto : dto.items()) {
            PedidoDetalle originalDet = origen.getPedidoDetalles().stream()
                    .filter(d -> d.getId().equals(itemDto.detalleId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("DETALLE NO ENCONTRADO: " + itemDto.detalleId()));

            int cantAMover = itemDto.cantidad();
            int cantOriginal = originalDet.getCantidad();

            if (cantAMover <= 0 || cantAMover > cantOriginal) {
                throw new RuntimeException(
                        "CANTIDAD A MOVER NO VÁLIDA PARA: " + originalDet.getProducto().getNombreProducto());
            }

            if (cantAMover == cantOriginal) {
                // Mover item completo
                originalDet.setPedido(nuevo);
                nuevosDetalles.add(originalDet);
            } else {
                // Split parcial
                // 1. Reducir original
                originalDet.setCantidad(cantOriginal - cantAMover);
                originalDet.setTotalLinea(
                        originalDet.getPrecioUnitario().multiply(new java.math.BigDecimal(originalDet.getCantidad())));

                // 2. Crear clon para el nuevo pedido
                PedidoDetalle clon = PedidoDetalle.builder()
                        .pedido(nuevo)
                        .producto(originalDet.getProducto())
                        .cantidad(cantAMover)
                        .precioUnitario(originalDet.getPrecioUnitario())
                        .totalLinea(originalDet.getPrecioUnitario().multiply(new java.math.BigDecimal(cantAMover)))
                        .observaciones(originalDet.getObservaciones())
                        .estadoPreparacion(originalDet.getEstadoPreparacion())
                        .estadoPago(originalDet.getEstadoPago())
                        .build();
                nuevosDetalles.add(clon);
            }
        }

        // Remover del origen los que se movieron completamente
        origen.getPedidoDetalles().removeIf(d -> d.getPedido() == nuevo);

        nuevo.setPedidoDetalles(nuevosDetalles);

        // 5. Recalcular y Guardar
        origen.calcularTotales();
        nuevo.calcularTotales();

        pedidoRepository.save(origen);
        return pedidoMapper.toDto(pedidoRepository.save(nuevo));
    }
}
