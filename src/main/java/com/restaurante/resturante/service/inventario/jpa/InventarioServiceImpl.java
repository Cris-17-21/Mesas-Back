package com.restaurante.resturante.service.inventario.jpa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.inventario.Inventario;
import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.dto.inventario.InventarioDto;
import com.restaurante.resturante.dto.inventario.MovimientoInventarioDto;
import com.restaurante.resturante.dto.inventario.MovimientoRequest;
import com.restaurante.resturante.domain.inventario.MovimientoInventario;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.repository.inventario.InventarioRepository;
import com.restaurante.resturante.repository.inventario.MovimientoInventarioRepository;
import com.restaurante.resturante.repository.inventario.ProductoRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.service.inventario.IInventarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements IInventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDto> listarProductosInventario(String sucursalId) {
        List<Inventario> inventarios = inventarioRepository.findAll().stream()
                .filter(i -> i.getSucursal() != null && i.getSucursal().getId().equals(sucursalId))
                .collect(Collectors.toList());
        Map<Integer, InventarioDto> productosMap = new LinkedHashMap<>();

        for (Inventario inventario : inventarios) {
            if (inventario == null)
                continue;

            Producto producto = inventario.getProducto();
            if (producto == null || producto.getIdProducto() == null || !Boolean.TRUE.equals(producto.getEstado())) {
                continue;
            }

            final Integer productoId = producto.getIdProducto();
            Proveedor proveedor = producto.getProveedor();

            if (proveedor == null || proveedor.getIdProveedor() == null) {
                continue;
            }

            productosMap.computeIfAbsent(productoId, id -> InventarioDto.builder()
                    .idProducto(productoId)
                    .nombreProducto(producto.getNombreProducto())
                    .costoCompra(producto.getCostoCompra())
                    .precioVenta(producto.getPrecioVenta())
                    .stockActual(inventario.getStockActual())
                    .stockMinimo(inventario.getStockMinimo())
                    .idProveedor(proveedor.getIdProveedor())
                    .nombreProveedor(proveedor.getRazonSocial())
                    .build());
        }

        return productosMap.values().stream()
                .sorted(Comparator.comparing(dto -> dto.getNombreProducto() != null ? dto.getNombreProducto() : "",
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedoresConInventario(String sucursalId) {
        return inventarioRepository.findDistinctProveedoresConInventario(sucursalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDto> listarProductosPorProveedor(Integer idProveedor, String sucursalId) {
        List<Inventario> inventarios = inventarioRepository
                .findByProductoProveedorIdProveedorAndProductoEstadoTrueAndSucursalId(idProveedor, sucursalId);

        Set<Integer> productosIncluidos = new HashSet<>();
        List<InventarioDto> dtos = new ArrayList<>();

        for (Inventario inventario : inventarios) {
            if (inventario == null)
                continue;

            Producto producto = inventario.getProducto();
            if (producto == null || producto.getIdProducto() == null)
                continue;

            if (!productosIncluidos.add(producto.getIdProducto()))
                continue;

            Proveedor proveedor = producto.getProveedor();

            dtos.add(InventarioDto.builder()
                    .idProducto(producto.getIdProducto())
                    .nombreProducto(producto.getNombreProducto())
                    .costoCompra(producto.getCostoCompra())
                    .precioVenta(producto.getPrecioVenta())
                    .stockActual(inventario.getStockActual())
                    .stockMinimo(inventario.getStockMinimo())
                    .idProveedor(proveedor != null ? proveedor.getIdProveedor() : null)
                    .nombreProveedor(proveedor != null ? proveedor.getRazonSocial() : null)
                    .build());
        }

        return dtos;
    }

    @Override
    @Transactional
    public MovimientoInventarioDto registrarMovimiento(MovimientoRequest request) {
        Producto producto = productoRepository.findById(request.idProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        Sucursal sucursal = sucursalRepository.findById(request.sucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        // Only track if product controls stock
        if (Boolean.TRUE.equals(producto.getControlarStock())) {
            Inventario inventario = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_Id(producto.getIdProducto(), sucursal.getId())
                    .orElseGet(() -> {
                        Inventario inv = Inventario.builder()
                                .producto(producto)
                                .sucursal(sucursal)
                                .stockActual(0)
                                .stockMinimo(producto.getStockMinimo() != null ? producto.getStockMinimo() : 5)
                                .build();
                        inv.setCreatedBy(request.usuarioId() != null ? request.usuarioId() : "SYSTEM");
                        return inv;
                    });

            if ("ENTRADA".equalsIgnoreCase(request.tipoMovimiento())) {
                inventario.setStockActual(inventario.getStockActual() + request.cantidad());
            } else if ("SALIDA".equalsIgnoreCase(request.tipoMovimiento())) {
                if (inventario.getStockActual() < request.cantidad() && !"PEDIDO".equals(request.motivo())) {
                    // Prevent manual outputs below zero if not a pedido/venta (sales usually can overdraw if not strictly forbidden, but let's be strict for manual)
                    throw new RuntimeException("Stock insuficiente para realizar la salida. Stock actual: " + inventario.getStockActual());
                }
                inventario.setStockActual(inventario.getStockActual() - request.cantidad());
            }

            inventarioRepository.save(inventario);
        }

        MovimientoInventario mov = MovimientoInventario.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(request.tipoMovimiento().toUpperCase())
                .cantidad(request.cantidad())
                .motivo(request.motivo())
                .usuarioId(request.usuarioId())
                .comprobante(request.comprobante())
                .build();
        
        mov = movimientoRepository.save(mov);

        return toMovimientoDto(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDto> obtenerHistorialMovimientos(String sucursalId) {
        return movimientoRepository.findBySucursal_IdOrderByFechaMovimientoDesc(sucursalId)
                .stream().map(this::toMovimientoDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDto> obtenerHistorialMovimientosPorProducto(Integer idProducto, String sucursalId) {
        return movimientoRepository.findByProducto_IdProductoAndSucursal_IdOrderByFechaMovimientoDesc(idProducto, sucursalId)
                .stream().map(this::toMovimientoDto).collect(Collectors.toList());
    }

    private MovimientoInventarioDto toMovimientoDto(MovimientoInventario mov) {
        return new MovimientoInventarioDto(
            mov.getIdMovimiento(),
            mov.getProducto().getIdProducto(),
            mov.getProducto().getNombreProducto(),
            mov.getSucursal().getId(),
            mov.getTipoMovimiento(),
            mov.getCantidad(),
            mov.getMotivo(),
            mov.getFechaMovimiento(),
            mov.getUsuarioId(),
            mov.getComprobante()
        );
    }
}
