package com.restaurante.resturante.service.inventario.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private final com.restaurante.resturante.repository.maestro.EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoDto> findByEmpresaId(String empresaId) {
        return categoriaRepository.findByEmpresaId(empresaId).stream()
                .map(categoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaProductoDto save(CategoriaProductoDto dto) {
        if (dto.empresaId() == null || dto.empresaId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "empresaId es obligatorio para crear una categoría");
        }
        com.restaurante.resturante.domain.maestros.Empresa empresa = empresaRepository
                .findById(dto.empresaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se encontró la empresa con ID: " + dto.empresaId() +
                                ". Por favor cierra sesión e ingresa de nuevo."));
        com.restaurante.resturante.domain.inventario.CategoriaProducto entity = categoriaMapper.toEntity(dto, empresa);
        return categoriaMapper.toDto(categoriaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaProductoDto findById(Integer id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public CategoriaProductoDto update(Integer id, CategoriaProductoDto dto) {
        return categoriaRepository.findById(id).map(existing -> {
            existing.setNombreCategoria(dto.nombreCategoria());
            // Usually we don't update empresa/sucursal unless necessary
            return categoriaMapper.toDto(categoriaRepository.save(existing));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoDto> findAll() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDto)
                .collect(Collectors.toList());
    }

}
