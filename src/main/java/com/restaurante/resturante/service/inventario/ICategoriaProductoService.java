package com.restaurante.resturante.service.inventario;

import java.util.List;

import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;

public interface ICategoriaProductoService {

    CategoriaProductoDto save(CategoriaProductoDto dto);

    CategoriaProductoDto findById(Integer id);

    CategoriaProductoDto update(Integer id, CategoriaProductoDto dto);

    void delete(Integer id);

    List<CategoriaProductoDto> findAll();

    List<CategoriaProductoDto> findByEmpresaId(String empresaId);
}
