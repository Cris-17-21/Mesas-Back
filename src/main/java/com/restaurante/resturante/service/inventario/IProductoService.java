package com.restaurante.resturante.service.inventario;

import java.util.List;
import java.util.Optional;

import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.dto.inventario.ProductoDto;

public interface IProductoService {
    List<ProductoDto> findAll();

    Optional<ProductoDto> findById(Integer id);

    Producto findEntityById(Integer id);

    ProductoDto save(ProductoDto dto);

    ProductoDto update(Integer id, ProductoDto dto);

}
