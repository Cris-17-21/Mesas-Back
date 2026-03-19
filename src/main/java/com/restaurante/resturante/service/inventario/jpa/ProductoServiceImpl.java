package com.restaurante.resturante.service.inventario.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.dto.inventario.ProductoDto;
import com.restaurante.resturante.mapper.inventario.ProductoDtoMapper;
import com.restaurante.resturante.repository.compras.ProveedorRepository;
import com.restaurante.resturante.repository.inventario.CategoriaProductoRepository;
import com.restaurante.resturante.repository.inventario.ProductoRepository;
import com.restaurante.resturante.service.inventario.IProductoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final com.restaurante.resturante.repository.inventario.TiposProductoRepository tiposRepository;
    private final ProductoDtoMapper productoMapper;
    private final com.restaurante.resturante.repository.venta.PedidoDetalleRepository pedidoDetalleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findAll() {
        return productoRepository.findByEstadoTrue().stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDto> findById(Integer id) {
        return productoRepository.findById(id)
                .map(productoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findEntityById(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ProductoDto save(ProductoDto dto) {
        CategoriaProducto categoria = null;
        if (dto.esPlato() != null && dto.esPlato()) {
            List<CategoriaProducto> categorias = categoriaRepository.findAll();
            if (!categorias.isEmpty()) {
                categoria = categorias.get(0);
            } else {
                throw new RuntimeException("Debe existir al menos una categoría en el sistema para registrar platos.");
            }
        } else if (dto.idCategoria() != null) {
            categoria = categoriaRepository.findById(dto.idCategoria()).orElse(null);
        }

        Proveedor proveedor = null;
        if (dto.idProveedor() != null) {
            proveedor = proveedorRepository.findById(dto.idProveedor()).orElse(null);
        }

        java.util.Set<com.restaurante.resturante.domain.inventario.TiposProducto> tipos = null;
        if (dto.idTipos() != null && !dto.idTipos().isEmpty()) {
            tipos = new java.util.HashSet<>(tiposRepository.findAllById(dto.idTipos()));
        }

        Producto entity = productoMapper.toEntity(dto, categoria, proveedor, tipos);
        return productoMapper.toDto(productoRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductoDto update(Integer id, ProductoDto dto) {
        // Fetch existing
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para actualizar"));

        CategoriaProducto categoria = null;
        if (dto.idCategoria() != null) {
            categoria = categoriaRepository.findById(dto.idCategoria()).orElse(null);
        }

        Proveedor proveedor = null;
        if (dto.idProveedor() != null) {
            proveedor = proveedorRepository.findById(dto.idProveedor()).orElse(null);
        }

        java.util.Set<com.restaurante.resturante.domain.inventario.TiposProducto> tipos = null;
        if (dto.idTipos() != null && !dto.idTipos().isEmpty()) {
            tipos = new java.util.HashSet<>(tiposRepository.findAllById(dto.idTipos()));
        }

        // Apply fields to existing entity
        existente.setNombreProducto(dto.nombreProducto());
        existente.setDescripcion(dto.descripcion());
        existente.setPrecioVenta(dto.precioVenta());
        existente.setCostoCompra(dto.costoCompra());
        existente.setCategoria(categoria);
        existente.setProveedor(proveedor);
        existente.setTipo(dto.tipo());
        existente.setTipos(tipos);
        existente.setPesoGramos(dto.pesoGramos());
        existente.setEstado(dto.estado() != null ? dto.estado() : existente.getEstado());
        existente.setImagen(dto.imagen());
        existente.setEsPlato(dto.esPlato() != null ? dto.esPlato() : existente.getEsPlato());
        existente.setHorarioDisponible(dto.horarioDisponible());
        existente.setFechaDisponible(dto.fechaDisponible());

        return productoMapper.toDto(productoRepository.save(existente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findByEmpresaId(String empresaId) {
        return productoRepository.findByEmpresaIdAndEstadoTrue(empresaId).stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setEstado(false);
        productoRepository.save(producto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findPlatosByEmpresaId(String empresaId) {
        return productoRepository.findByEstadoTrueAndEsPlatoTrue().stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto> getPlatoSalesHistory(String empresaId) {
        List<Producto> platos = productoRepository.findByEstadoTrueAndEsPlatoTrue();
        List<com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto> history = new java.util.ArrayList<>();

        for (Producto plato : platos) {
            List<com.restaurante.resturante.domain.ventas.PedidoDetalle> detalles = pedidoDetalleRepository.findByProductoIdProducto(plato.getIdProducto());
            int manana = 0;
            int tarde = 0;
            int noche = 0;
            int total = 0;

            for (com.restaurante.resturante.domain.ventas.PedidoDetalle detalle : detalles) {
                if (detalle.getPedido() != null && !"ANULADO".equals(detalle.getPedido().getEstado())) {
                    java.time.LocalDateTime fecha = detalle.getPedido().getFechaCreacion();
                    if (fecha != null) {
                        int hour = fecha.getHour();
                        if (hour >= 6 && hour < 14) {
                            manana += detalle.getCantidad();
                        } else if (hour >= 14 && hour < 19) {
                            tarde += detalle.getCantidad();
                        } else {
                            // Noche: 19:00 a 5:59
                            noche += detalle.getCantidad();
                        }
                    }
                    total += detalle.getCantidad();
                }
            }

            String nombreCategoria = plato.getCategoria() != null ? plato.getCategoria().getNombreCategoria() : "Sin Categoría";
            if (manana > 0) {
                history.add(new com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto(
                    plato.getIdProducto(), plato.getNombreProducto(), nombreCategoria, "Mañana", manana, plato.getPrecioVenta()
                ));
            }
            if (tarde > 0) {
                history.add(new com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto(
                    plato.getIdProducto(), plato.getNombreProducto(), nombreCategoria, "Tarde", tarde, plato.getPrecioVenta()
                ));
            }
            if (noche > 0) {
                history.add(new com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto(
                    plato.getIdProducto(), plato.getNombreProducto(), nombreCategoria, "Noche", noche, plato.getPrecioVenta()
                ));
            }
        }
        
        // Ordenar por cantidad vendida
        history.sort((a, b) -> Integer.compare(b.cantidadVendida(), a.cantidadVendida()));

        return history;
    }
}
