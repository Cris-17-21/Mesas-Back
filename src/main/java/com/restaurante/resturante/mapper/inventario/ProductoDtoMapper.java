package com.restaurante.resturante.mapper.inventario;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.inventario.Producto;
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
                producto.getPesoGramos(),

                producto.getEstado(),
                producto.getImagen());
    }

    public Producto toEntity(ProductoDto dto, CategoriaProducto categoria, Proveedor proveedor) {
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
                .pesoGramos(dto.pesoGramos())

                .estado(dto.estado())
                .imagen(dto.imagen())
                .build();
    }
}
