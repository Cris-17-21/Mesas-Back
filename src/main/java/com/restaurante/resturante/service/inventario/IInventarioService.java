package com.restaurante.resturante.service.inventario;

import java.util.List;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.dto.inventario.InventarioDto;

public interface IInventarioService {

    List<InventarioDto> listarProductosInventario();

    List<Proveedor> listarProveedoresConInventario();

    List<InventarioDto> listarProductosPorProveedor(Integer idProveedor);
}
