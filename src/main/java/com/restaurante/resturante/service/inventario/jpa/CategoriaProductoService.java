package com.restaurante.resturante.service.inventario.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;
import com.restaurante.resturante.mapper.inventario.CategoriaProductoDtoMapper;
import com.restaurante.resturante.repository.inventario.CategoriaProductoRepository;
import com.restaurante.resturante.service.inventario.ICategoriaProductoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaProductoService implements ICategoriaProductoService {

    private final CategoriaProductoRepository categoriaRepository;

    private final CategoriaProductoDtoMapper categoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoDto> findByEmpresaId(String empresaId) {
        return categoriaRepository.findByEmpresaId(empresaId).stream()
                .map(categoriaMapper::toDto)
                .collect(Collectors.toList());
    }

}
