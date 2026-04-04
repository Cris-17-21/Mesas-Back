package com.restaurante.resturante.mapper.inventario;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.inventario.ProductoDto;

@Component
public class ProductoDtoMapper {

    public ProductoDto toDto(Producto producto) {
        if (producto == null)
            return null;
        return new ProductoDto(
                producto.getIdProducto(),
                producto.getNombreProducto(),
                producto.getDescripcion(),
                producto.getPrecioVenta(),
                producto.getCostoCompra(),
                producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombreCategoria() : null,
                producto.getProveedor() != null ? producto.getProveedor().getIdProveedor() : null,
                producto.getProveedor() != null ? producto.getProveedor().getRazonSocial() : null,
                producto.getTipo(),
                producto.getTipos() != null
                        ? producto.getTipos().stream()
                                .map(com.restaurante.resturante.domain.inventario.TiposProducto::getIdTipo)
                                .collect(java.util.stream.Collectors.toList())
                        : null,
                producto.getPesoGramos(),

                null, // fechaRegistro set to null temporarily to restore stability
                0, // stock computed via overload or 0
                producto.getSucursal() != null ? producto.getSucursal().getId() : null, // sucursalId provided by entity

                producto.getEstado(),
                producto.getImagen(),
                
                producto.getEsPlato(),
                producto.getHorarioDisponible(),
                producto.getFechaDisponible());
    }

    public ProductoDto toDto(Producto producto, Integer stockActual) {
        if (producto == null) return null;
        ProductoDto dto = toDto(producto);
        return new ProductoDto(
                dto.idProducto(), dto.nombreProducto(), dto.descripcion(), dto.precioVenta(), dto.costoCompra(),
                dto.idCategoria(), dto.nombreCategoria(), dto.idProveedor(), dto.razonSocialProveedor(),
                dto.tipo(), dto.idTipos(), dto.pesoGramos(), dto.fechaRegistro(), 
                stockActual != null ? stockActual : 0, 
                dto.sucursalId(), dto.estado(), dto.imagen(), dto.esPlato(), dto.horarioDisponible(), dto.fechaDisponible()
        );
    }

    public Producto toEntity(ProductoDto dto, CategoriaProducto categoria, Proveedor proveedor,
            java.util.Set<com.restaurante.resturante.domain.inventario.TiposProducto> tipos, Sucursal sucursal) {
        if (dto == null)
            return null;

        return Producto.builder()
                .idProducto(dto.idProducto())
                .nombreProducto(dto.nombreProducto())
                .descripcion(dto.descripcion())
                .precioVenta(dto.precioVenta())
                .costoCompra(dto.costoCompra())
                .categoria(categoria)
                .proveedor(proveedor)
                .tipo(dto.tipo())
                .tipos(tipos)
                .sucursal(sucursal)
                .pesoGramos(dto.pesoGramos())

                .estado(dto.estado())
                .imagen(dto.imagen())
                .esPlato(dto.esPlato())
                .horarioDisponible(dto.horarioDisponible())
                .fechaDisponible(dto.fechaDisponible())
                .build();
    }
}
