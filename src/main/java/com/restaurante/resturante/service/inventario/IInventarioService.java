package com.restaurante.resturante.service.inventario;

import java.util.List;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.dto.inventario.InventarioDto;

public interface IInventarioService {

    List<InventarioDto> listarProductosInventario(String sucursalId);

    List<Proveedor> listarProveedoresConInventario(String sucursalId);

    List<InventarioDto> listarProductosPorProveedor(Integer idProveedor, String sucursalId);
}
