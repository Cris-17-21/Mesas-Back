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
import com.restaurante.resturante.repository.maestro.SucursalRepository;
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
    private final com.restaurante.resturante.repository.inventario.InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findAll() {
        return productoRepository.findByEstadoTrue().stream()
                .map(p -> {
                    Integer stock = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(p.getIdProducto(), p.getSucursal() != null ? p.getSucursal().getId() : null)
                            .map(com.restaurante.resturante.domain.inventario.Inventario::getStockActual)
                            .orElse(0);
                    return productoMapper.toDto(p, stock);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDto> findById(Integer id) {
        return productoRepository.findById(id)
                .map(p -> {
                    Integer stock = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(p.getIdProducto(), p.getSucursal() != null ? p.getSucursal().getId() : null)
                            .map(com.restaurante.resturante.domain.inventario.Inventario::getStockActual)
                            .orElse(0);
                    return productoMapper.toDto(p, stock);
                });
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

        com.restaurante.resturante.domain.maestros.Sucursal sucursal = null;
        if (dto.sucursalId() != null) {
            sucursal = sucursalRepository.findById(dto.sucursalId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + dto.sucursalId()));
        } else {
            throw new RuntimeException("sucursalId es requerido para guardar un producto");
        }

        Producto entity = productoMapper.toEntity(dto, categoria, proveedor, tipos, sucursal);
        Producto saved = productoRepository.save(entity);
        
        // Ensure stock record exists if stock provided
        if (dto.stock() != null) {
            com.restaurante.resturante.domain.inventario.Inventario inv = com.restaurante.resturante.domain.inventario.Inventario.builder()
                    .producto(saved)
                    .sucursal(sucursal)
                    .stockActual(dto.stock())
                    .stockMinimo(5)
                    .build();
            inv.setCreatedBy("SYSTEM");
            inventarioRepository.save(inv);
        }

        return productoMapper.toDto(saved, dto.stock() != null ? dto.stock() : 0);
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

        // Apply fields to existing entity conditionally to avoid wiping non-DTO fields like 'tipo'
        if (dto.nombreProducto() != null) existente.setNombreProducto(dto.nombreProducto());
        if (dto.descripcion() != null) existente.setDescripcion(dto.descripcion());
        if (dto.precioVenta() != null) existente.setPrecioVenta(dto.precioVenta());
        if (dto.costoCompra() != null) existente.setCostoCompra(dto.costoCompra());
        if (categoria != null) existente.setCategoria(categoria);
        if (dto.idProveedor() != null) existente.setProveedor(proveedor); // only reset if explicit in form logic or handle if null means unassign, but form sends null when unchanged or unselected. Let's just set it. 
        existente.setProveedor(proveedor); // form dropdown maps null to empty. It's safe to overwrite.
        if (dto.tipo() != null) existente.setTipo(dto.tipo());
        if (tipos != null) existente.setTipos(tipos);
        if (dto.pesoGramos() != null) existente.setPesoGramos(dto.pesoGramos());
        if (dto.estado() != null) existente.setEstado(dto.estado());
        if (dto.imagen() != null) existente.setImagen(dto.imagen());
        if (dto.esPlato() != null) existente.setEsPlato(dto.esPlato());
        if (dto.horarioDisponible() != null) existente.setHorarioDisponible(dto.horarioDisponible());
        if (dto.fechaDisponible() != null) existente.setFechaDisponible(dto.fechaDisponible());

        Producto saved = productoRepository.save(existente);
        Integer finalStock = 0;

        // Update Stock if provided
        if (dto.stock() != null && dto.sucursalId() != null) {
            com.restaurante.resturante.domain.inventario.Inventario inv = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(saved.getIdProducto(), dto.sucursalId())
                    .orElseGet(() -> {
                        com.restaurante.resturante.domain.maestros.Sucursal sucursalObj = sucursalRepository.findById(dto.sucursalId())
                            .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con id: " + dto.sucursalId()));
                        com.restaurante.resturante.domain.inventario.Inventario newInv = com.restaurante.resturante.domain.inventario.Inventario.builder()
                                .producto(saved)
                                .sucursal(sucursalObj)
                                .stockActual(0)
                                .stockMinimo(5)
                                .build();
                        newInv.setCreatedBy("SYSTEM");
                        return newInv;
                    });
            inv.setStockActual(dto.stock());
            inventarioRepository.save(inv);
            finalStock = dto.stock();
        } else {
            finalStock = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(saved.getIdProducto(), dto.sucursalId())
                    .map(com.restaurante.resturante.domain.inventario.Inventario::getStockActual)
                    .orElse(0);
        }

        return productoMapper.toDto(saved, finalStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findBySucursalId(String sucursalId) {
        List<Producto> productos = productoRepository.findBySucursal_Id(sucursalId);
        System.out.println("DEBUG: Encontrados " + productos.size() + " productos para sucursal " + sucursalId);
        return productos.stream()
                .map(p -> {
                    Integer stock = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(p.getIdProducto(), sucursalId)
                            .map(com.restaurante.resturante.domain.inventario.Inventario::getStockActual)
                            .orElse(0);
                    return productoMapper.toDto(p, stock);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findByEmpresaId(String empresaId) {
        return productoRepository.findBySucursal_Empresa_IdAndEstadoTrue(empresaId).stream()
                .map(p -> {
                    Integer stock = inventarioRepository.findByProducto_IdProductoAndSucursal_Id(p.getIdProducto(), p.getSucursal() != null ? p.getSucursal().getId() : null)
                            .map(com.restaurante.resturante.domain.inventario.Inventario::getStockActual)
                            .orElse(0);
                    return productoMapper.toDto(p, stock);
                })
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
    public List<ProductoDto> findPlatosBySucursalId(String sucursalId) {
        return productoRepository.findBySucursal_IdAndEstadoTrueAndEsPlatoTrue(sucursalId).stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.restaurante.resturante.dto.inventario.PlatoSalesHistoryDto> getPlatoSalesHistory(String sucursalId) {
        List<Producto> platos = productoRepository.findBySucursal_IdAndEstadoTrueAndEsPlatoTrue(sucursalId);
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
