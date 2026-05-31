package com.restaurante.resturante.service.venta.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.maestro.UnionMesaRequest;
import com.restaurante.resturante.dto.venta.PedidoDetalleRequestDto;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResumenDto;
import com.restaurante.resturante.dto.venta.PreCuentaDto;
import com.restaurante.resturante.dto.venta.PagoMixtoItemDto;
import com.restaurante.resturante.dto.venta.RegistrarPagoDto;
import com.restaurante.resturante.dto.venta.SepararCuentaDto;
import com.restaurante.resturante.mapper.venta.PedidoDetalleMapper;
import com.restaurante.resturante.mapper.venta.PedidoMapper;
import com.restaurante.resturante.repository.inventario.ProductoRepository;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;
import com.restaurante.resturante.service.maestros.IMesaService;
import com.restaurante.resturante.service.venta.IPedidoService;
import com.restaurante.resturante.service.inventario.IInventarioService;
import com.restaurante.resturante.dto.inventario.MovimientoRequest;

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
        private final com.restaurante.resturante.repository.venta.PedidoPagoRepository pagoRepository;
        private final com.restaurante.resturante.repository.maestro.MedioPagoRepository medioPagoRepository;
        private final IInventarioService inventarioService;
        private final MovimientoCajaRepository movimientoRepository;
        private final com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository facturacionRepository;

        @Override
        @Transactional
        public PedidoResponseDto crearPedido(PedidoRequestDto dto) {
                // 0. Validar Teléfono (hasta 9 dígitos numéricos)
                if (dto.telefono() != null && !dto.telefono().isBlank()) {
                        if (!dto.telefono().matches("^[0-9]{1,9}$")) {
                                throw new RuntimeException("EL TELÉFONO DEBE TENER HASTA 9 DÍGITOS Y CONTENER SOLO NÚMEROS");
                        }
                }

                // 1. Validar Caja Abierta
                var caja = cajaRepository
                                .findByUserIdAndSucursalIdAndEstado(dto.usuarioId(), dto.sucursalId(), "ABIERTA")
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
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "PRODUCTO NO ENCONTRADO: " + d.productoId()));
                                        det.setProducto(producto);
                                        det.setPrecioUnitario(producto.getPrecioVenta());
                                        det.setTotalLinea(producto.getPrecioVenta()
                                                        .multiply(new java.math.BigDecimal(det.getCantidad())));
                                        return det;
                                }).toList();

                pedido.setPedidoDetalles(detalles);
                pedido.calcularTotales();

                // 4. Cambiar estado de mesa y asociarla
                if (dto.mesaId() != null && !dto.mesaId().isBlank()) {
                        Mesa mesa = mesaRepository.findById(dto.mesaId())
                                        .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + dto.mesaId()));
                        
                        // Si la mesa seleccionada es una mesa secundaria (unida), asociamos el pedido a la principal
                        if (mesa.getPrincipal() != null) {
                                mesa = mesa.getPrincipal();
                        }
                        
                        pedido.setMesa(mesa);
                        mesaService.cambiarEstado(mesa.getId(), "OCUPADA");
                }
                
                Pedido savedPedido = pedidoRepository.save(pedido);

                // 5. Descontar stock (Hook Almacén)
                for (PedidoDetalle d : detalles) {
                        if (Boolean.TRUE.equals(d.getProducto().getControlarStock())) {
                                MovimientoRequest movReq = new MovimientoRequest(
                                                d.getProducto().getIdProducto(),
                                                sucursal.getId(),
                                                "SALIDA",
                                                d.getCantidad(),
                                                "PEDIDO",
                                                usuario.getId(),
                                                savedPedido.getCodigoPedido()
                                );
                                inventarioService.registrarMovimiento(movReq);
                        }
                }

                return pedidoMapper.toDto(savedPedido);
        }

        @Override
        @Transactional
        public PedidoResponseDto actualizarDetalles(String pedidoId, List<PedidoDetalleRequestDto> nuevosDetalles) {
                Pedido pedido = pedidoRepository.findById(pedidoId)
                                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                if (!"ABIERTO".equals(pedido.getEstado())) {
                        throw new RuntimeException("EL PEDIDO YA NO ESTÁ ABIERTO");
                }
                
                List<PedidoDetalle> extras = new java.util.ArrayList<>();

                for (PedidoDetalleRequestDto d : nuevosDetalles) {
                        Integer prodId = Integer.parseInt(d.productoId());

                        java.util.Optional<PedidoDetalle> existingDetailOpt = pedido.getPedidoDetalles().stream()
                                        .filter(det -> det.getProducto().getIdProducto().equals(prodId))
                                        .findFirst();

                        if (existingDetailOpt.isPresent()) {
                                PedidoDetalle det = existingDetailOpt.get();
                                det.setCantidad(det.getCantidad() + d.cantidad());
                                det.setTotalLinea(det.getPrecioUnitario().multiply(new java.math.BigDecimal(det.getCantidad())));
                        } else {
                                PedidoDetalle det = detalleMapper.toEntity(d);
                                det.setPedido(pedido);

                                Producto producto = productoRepository.findById(prodId)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "PRODUCTO NO ENCONTRADO: " + d.productoId()));
                                det.setProducto(producto);
                                det.setPrecioUnitario(producto.getPrecioVenta());
                                det.setTotalLinea(producto.getPrecioVenta()
                                                .multiply(new java.math.BigDecimal(det.getCantidad())));
                                extras.add(det);
                        }
                }

                if (!extras.isEmpty()) {
                        pedido.getPedidoDetalles().addAll(extras);
                }
                pedido.calcularTotales(); // Recalcula total_final

                Pedido savedPedido = pedidoRepository.save(pedido);

                // Descontar stock (Hook Almacén)
                for (PedidoDetalleRequestDto d : nuevosDetalles) {
                        Integer prodId = Integer.parseInt(d.productoId());
                        Producto producto = productoRepository.findById(prodId)
                                        .orElseThrow(() -> new RuntimeException("PRODUCTO NO ENCONTRADO: " + d.productoId()));
                        if (Boolean.TRUE.equals(producto.getControlarStock())) {
                                MovimientoRequest movReq = new MovimientoRequest(
                                                producto.getIdProducto(),
                                                pedido.getSucursal().getId(),
                                                "SALIDA",
                                                d.cantidad(),
                                                "PEDIDO",
                                                pedido.getUser().getId(),
                                                savedPedido.getCodigoPedido()
                                );
                                inventarioService.registrarMovimiento(movReq);
                        }
                }

                return pedidoMapper.toDto(savedPedido);
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
        public void registrarPago(RegistrarPagoDto dto) {
                Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                java.math.BigDecimal totalFinal = pedido.getTotalFinal();
                java.math.BigDecimal pagado = pedido.getMontoPagado();
                java.math.BigDecimal restante = totalFinal.subtract(pagado);
                java.math.BigDecimal montoARegistrar = (dto.monto() != null && dto.monto().compareTo(java.math.BigDecimal.ZERO) > 0) 
                                ? dto.monto() : restante;

                if (montoARegistrar.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("EL PEDIDO YA SE ENCUENTRA TOTALMENTE PAGADO");
                }

                String metodosRaw = dto.metodoPago();
                String[] metodos = metodosRaw.split(",");

                if (metodos.length > 1) {
                        // Pago Mixto: dividimos el monto equitativamente para asociar a los medios correspondientes
                        java.math.BigDecimal montoPorMetodo = montoARegistrar.divide(
                                        java.math.BigDecimal.valueOf(metodos.length), 2, java.math.RoundingMode.HALF_UP);
                        
                        for (int i = 0; i < metodos.length; i++) {
                                String name = metodos[i].trim();
                                com.restaurante.resturante.domain.maestros.MedioPago medioPago = medioPagoRepository
                                                .findByNombreAndEmpresaIdAndIsActiveTrue(name, pedido.getSucursal().getEmpresa().getId())
                                                .orElseThrow(() -> new RuntimeException("MEDIO DE PAGO NO ENCONTRADO: " + name));

                                // El último pago absorbe el residuo del redondeo
                                java.math.BigDecimal montoFinal = (i == metodos.length - 1) 
                                                ? montoARegistrar.subtract(montoPorMetodo.multiply(java.math.BigDecimal.valueOf(metodos.length - 1))) 
                                                : montoPorMetodo;

                                com.restaurante.resturante.domain.ventas.PedidoPago pago = com.restaurante.resturante.domain.ventas.PedidoPago
                                                .builder()
                                                .pedido(pedido)
                                                .medioPago(medioPago)
                                                .monto(montoFinal)
                                                .referenciaPago(dto.referencia())
                                                .fechaPago(LocalDateTime.now())
                                                .cajaTurno(pedido.getCajaTurno())
                                                .build();
                                pagoRepository.save(pago);

                                // Registrar movimiento en caja
                                if (pedido.getCajaTurno() != null && debeRegistrarMovimientoCaja(pedido.getId())) {
                                        String tipoEnt = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "MESA";
                                        String infoMesa = (pedido.getMesa() != null) ? " - " + pedido.getMesa().getCodigoMesa() : "";
                                        com.restaurante.resturante.domain.ventas.MovimientoCaja mov = com.restaurante.resturante.domain.ventas.MovimientoCaja.builder()
                                                        .cajaTurno(pedido.getCajaTurno())
                                                        .usuario(pedido.getCajaTurno().getUser() != null ? pedido.getCajaTurno().getUser() : pedido.getUser())
                                                        .tipo(com.restaurante.resturante.domain.ventas.TipoMovimiento.INGRESO)
                                                        .monto(montoFinal)
                                                        .descripcion("Venta " + pedido.getCodigoPedido() + " (" + tipoEnt + infoMesa + ") - Medio: " + medioPago.getNombre())
                                                        .fecha(LocalDateTime.now())
                                                        .esEfectivo(medioPago.isEsEfectivo())
                                                        .build();
                                        movimientoRepository.save(mov);
                                }
                        }
                } else {
                        // Pago Simple
                        String name = metodosRaw.trim();
                        com.restaurante.resturante.domain.maestros.MedioPago medioPago = medioPagoRepository
                                        .findByNombreAndEmpresaIdAndIsActiveTrue(name, pedido.getSucursal().getEmpresa().getId())
                                        .orElseThrow(() -> new RuntimeException("MEDIO DE PAGO NO ENCONTRADO: " + name));

                        com.restaurante.resturante.domain.ventas.PedidoPago pago = com.restaurante.resturante.domain.ventas.PedidoPago
                                        .builder()
                                        .pedido(pedido)
                                        .medioPago(medioPago)
                                        .monto(montoARegistrar)
                                        .referenciaPago(dto.referencia())
                                        .fechaPago(LocalDateTime.now())
                                        .cajaTurno(pedido.getCajaTurno())
                                        .build();
                        pagoRepository.save(pago);

                        // Registrar movimiento en caja
                        if (pedido.getCajaTurno() != null && debeRegistrarMovimientoCaja(pedido.getId())) {
                                String tipoEnt = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "MESA";
                                String infoMesa = (pedido.getMesa() != null) ? " - " + pedido.getMesa().getCodigoMesa() : "";
                                com.restaurante.resturante.domain.ventas.MovimientoCaja mov = com.restaurante.resturante.domain.ventas.MovimientoCaja.builder()
                                                .cajaTurno(pedido.getCajaTurno())
                                                .usuario(pedido.getCajaTurno().getUser() != null ? pedido.getCajaTurno().getUser() : pedido.getUser())
                                                .tipo(com.restaurante.resturante.domain.ventas.TipoMovimiento.INGRESO)
                                                .monto(montoARegistrar)
                                                .descripcion("Venta " + pedido.getCodigoPedido() + " (" + tipoEnt + infoMesa + ") - Medio: " + medioPago.getNombre())
                                                .fecha(LocalDateTime.now())
                                                .esEfectivo(medioPago.isEsEfectivo())
                                                .build();
                                        movimientoRepository.save(mov);
                        }
                }

                // 2. Verificar si el pedido está pagado completamente
                if (pedido.estaTotalmentePagado() || pedido.estaPagadoCompletamente()) {
                        pedido.setEstado("PAGADO");
                        pedido.setFechaCierre(LocalDateTime.now());

                        // 3. Liberar Mesas
                        if (pedido.getMesa() != null) {
                                String mesaId = pedido.getMesa().getId();
                                mesaService.separarMesas(mesaId);
                                mesaService.cambiarEstado(mesaId, "LIBRE");
                        }
                }

                pedidoRepository.save(pedido);
        }

        @Override
        @Transactional
        public void registrarPagoMixto(String pedidoId, List<PagoMixtoItemDto> pagos) {
                Pedido pedido = pedidoRepository.findById(pedidoId)
                                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                if (pedido.estaTotalmentePagado()) {
                        throw new RuntimeException("EL PEDIDO YA SE ENCUENTRA TOTALMENTE PAGADO");
                }

                if (pedido.getPagos() == null) {
                        pedido.setPagos(new java.util.ArrayList<>());
                }

                for (PagoMixtoItemDto item : pagos) {
                        if (item.monto() == null || item.monto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                                continue;
                        }
                        com.restaurante.resturante.domain.maestros.MedioPago medioPago = medioPagoRepository
                                        .findByIdAndEmpresaIdAndIsActiveTrue(item.medioPagoId(), pedido.getSucursal().getEmpresa().getId())
                                        .orElseThrow(() -> new RuntimeException("MEDIO DE PAGO NO ENCONTRADO O INACTIVO"));

                        com.restaurante.resturante.domain.ventas.PedidoPago pago = com.restaurante.resturante.domain.ventas.PedidoPago
                                        .builder()
                                        .pedido(pedido)
                                        .medioPago(medioPago)
                                        .monto(item.monto())
                                        .fechaPago(LocalDateTime.now())
                                        .cajaTurno(pedido.getCajaTurno())
                                        .build();

                        pagoRepository.save(pago);

                        // Registrar movimiento en caja
                        if (pedido.getCajaTurno() != null && debeRegistrarMovimientoCaja(pedido.getId())) {
                                String tipoEnt = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "MESA";
                                String infoMesa = (pedido.getMesa() != null) ? " - " + pedido.getMesa().getCodigoMesa() : "";
                                com.restaurante.resturante.domain.ventas.MovimientoCaja mov = com.restaurante.resturante.domain.ventas.MovimientoCaja.builder()
                                                .cajaTurno(pedido.getCajaTurno())
                                                .usuario(pedido.getCajaTurno().getUser() != null ? pedido.getCajaTurno().getUser() : pedido.getUser())
                                                .tipo(com.restaurante.resturante.domain.ventas.TipoMovimiento.INGRESO)
                                                .monto(item.monto())
                                                .descripcion("Venta " + pedido.getCodigoPedido() + " (" + tipoEnt + infoMesa + ") - Medio: " + medioPago.getNombre())
                                                .fecha(LocalDateTime.now())
                                                .esEfectivo(medioPago.isEsEfectivo())
                                                .build();
                                movimientoRepository.save(mov);
                        }

                        pedido.getPagos().add(pago);
                }

                // 2. Verificar si el pedido está pagado completamente
                if (pedido.estaTotalmentePagado() || pedido.estaPagadoCompletamente()) {
                        pedido.setEstado("PAGADO");
                        pedido.setFechaCierre(LocalDateTime.now());

                        // 3. Liberar Mesas
                        if (pedido.getMesa() != null) {
                                String mesaId = pedido.getMesa().getId();
                                mesaService.separarMesas(mesaId);
                                mesaService.cambiarEstado(mesaId, "LIBRE");
                        }
                }

                pedidoRepository.save(pedido);
        }

        @Override
        @Transactional(readOnly = true)
        public List<PedidoResumenDto> listarPedidosPorTipo(String sucursalId, String tipoEntrega) {
                return pedidoRepository.findBySucursalIdAndTipoEntregaAndEstado(sucursalId, tipoEntrega, "PENDIENTE")
                                .stream()
                                .map(pedidoMapper::toResumenDto)
                                .collect(Collectors.toList());
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
        @Transactional(readOnly = true)
        public List<Pedido> findPedidosActivos(String sucursalId) {
                return pedidoRepository.findBySucursalIdAndEstado(sucursalId, "ABIERTO");
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
                                        .orElseThrow(() -> new RuntimeException(
                                                        "DETALLE NO ENCONTRADO: " + itemDto.detalleId()));

                        int cantAMover = itemDto.cantidad();
                        int cantOriginal = originalDet.getCantidad();

                        if (cantAMover <= 0 || cantAMover > cantOriginal) {
                                throw new RuntimeException(
                                                "CANTIDAD A MOVER NO VÁLIDA PARA: "
                                                                + originalDet.getProducto().getNombreProducto());
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
                                                originalDet.getPrecioUnitario().multiply(
                                                                new java.math.BigDecimal(originalDet.getCantidad())));

                                // 2. Crear clon para el nuevo pedido
                                PedidoDetalle clon = PedidoDetalle.builder()
                                                .pedido(nuevo)
                                                .producto(originalDet.getProducto())
                                                .cantidad(cantAMover)
                                                .precioUnitario(originalDet.getPrecioUnitario())
                                                .totalLinea(originalDet.getPrecioUnitario()
                                                                .multiply(new java.math.BigDecimal(cantAMover)))
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

        @Override
        @Transactional(readOnly = true)
        public PreCuentaDto generarPreCuenta(String pedidoId) {
                Pedido pedido = pedidoRepository.findById(pedidoId)
                        .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                List<PreCuentaDto.PreCuentaDetalleDto> detalles = pedido.getPedidoDetalles().stream()
                        .map(d -> new PreCuentaDto.PreCuentaDetalleDto(
                                d.getProducto().getNombreProducto(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getTotalLinea()))
                        .toList();

                return new PreCuentaDto(
                                pedido.getId(),
                                pedido.getCodigoPedido(),
                                pedido.getMesa() != null ? pedido.getMesa().getCodigoMesa() : "LLEVAR/DELIVERY",
                                pedido.getUser().getUsername(),
                                pedido.getFechaCreacion(),
                                detalles,
                                pedido.getTotalProductos(),
                                pedido.getDescuentoGlobal(),
                                pedido.getTotalFinal());
        }

        @Override
        @Transactional
        public PedidoResponseDto actualizarEstadoPreparacion(String detalleId, String estadoPreparacion) {
                PedidoDetalle detalle = pedidoRepository.findDetalleById(detalleId)
                        .orElseThrow(() -> new RuntimeException("DETALLE DE PEDIDO NO ENCONTRADO"));
                
                // Validate estadoPreparacion if needed
                List<String> estadosValidos = List.of("PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO");
                if (!estadosValidos.contains(estadoPreparacion)) {
                        throw new IllegalArgumentException("ESTADO DE PREPARACIÓN NO VÁLIDO: " + estadoPreparacion);
                }
                
                detalle.setEstadoPreparacion(estadoPreparacion);
                pedidoRepository.save(detalle.getPedido()); // Save the pedido to cascade
                
                return pedidoMapper.toDto(detalle.getPedido());
        }

        @Override
        @Transactional(readOnly = true)
        public List<Pedido> findBySucursalIdAndDetallesEstadoPreparacion(String sucursalId, String estadoPreparacion) {
                return pedidoRepository.findBySucursalIdAndDetallesEstadoPreparacion(sucursalId, estadoPreparacion);
        }

        @Override
        @Transactional
        public PedidoResponseDto actualizarPreciosDetalles(String pedidoId, List<com.restaurante.resturante.dto.venta.ActualizarPrecioDetalleDto> precios) {
                Pedido pedido = pedidoRepository.findById(pedidoId)
                                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

                if (!"ABIERTO".equals(pedido.getEstado())) {
                        throw new RuntimeException("EL PEDIDO YA NO ESTÁ ABIERTO");
                }

                for (com.restaurante.resturante.dto.venta.ActualizarPrecioDetalleDto item : precios) {
                        PedidoDetalle detalle = pedido.getPedidoDetalles().stream()
                                        .filter(d -> d.getId().equals(item.detalleId()))
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("DETALLE NO ENCONTRADO: " + item.detalleId()));

                        detalle.setPrecioUnitario(item.precioUnitario());
                        detalle.setTotalLinea(item.precioUnitario().multiply(new java.math.BigDecimal(detalle.getCantidad())));
                }

                pedido.calcularTotales();
                Pedido savedPedido = pedidoRepository.save(pedido);
                return pedidoMapper.toDto(savedPedido);
        }

        private boolean debeRegistrarMovimientoCaja(String pedidoId) {
                var compOpt = facturacionRepository.findByPedidoId(pedidoId);
                if (compOpt.isEmpty()) {
                        return true;
                }
                return "ACEPTADO".equals(compOpt.get().getEstadoSunat());
        }
}

