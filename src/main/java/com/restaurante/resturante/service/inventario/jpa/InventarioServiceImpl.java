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
import com.restaurante.resturante.repository.inventario.InventarioRepository;
import com.restaurante.resturante.service.inventario.IInventarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements IInventarioService {

    private final InventarioRepository inventarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDto> listarProductosInventario() {
        List<Inventario> inventarios = inventarioRepository.findAll();
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
    public List<Proveedor> listarProveedoresConInventario() {
        return inventarioRepository.findDistinctProveedoresConInventario();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDto> listarProductosPorProveedor(Integer idProveedor) {
        List<Inventario> inventarios = inventarioRepository
                .findByProductoProveedorIdProveedorAndProductoEstadoTrue(idProveedor);

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
}
