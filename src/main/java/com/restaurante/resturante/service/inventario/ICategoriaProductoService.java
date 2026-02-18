package com.restaurante.resturante.service.inventario;

import java.util.List;

import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;

public interface ICategoriaProductoService {

    List<CategoriaProductoDto> findByEmpresaId(String empresaId);
}
