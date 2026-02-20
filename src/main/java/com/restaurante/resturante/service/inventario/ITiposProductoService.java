package com.restaurante.resturante.service.inventario;

import java.util.List;
import com.restaurante.resturante.domain.inventario.TiposProducto;

public interface ITiposProductoService {
    List<TiposProducto> findAll();

    TiposProducto findById(Integer id);

    TiposProducto save(TiposProducto tipo);

    void delete(Integer id);

    List<TiposProducto> findByCategoryId(Integer idCategoria);
}
