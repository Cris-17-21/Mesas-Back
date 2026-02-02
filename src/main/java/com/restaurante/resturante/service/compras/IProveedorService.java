package com.restaurante.resturante.service.compras;

import java.util.List;
import java.util.Optional;

import com.restaurante.resturante.dto.compras.ProveedorDto;
import com.restaurante.resturante.domain.compras.Proveedor;

public interface IProveedorService {
    List<ProveedorDto> findAll();

    Optional<ProveedorDto> findById(Integer id);

    Proveedor findEntityById(Integer id);

    ProveedorDto save(ProveedorDto proveedorDto);

    ProveedorDto update(Integer id, ProveedorDto proveedorDto);

    void delete(Integer id);
}
