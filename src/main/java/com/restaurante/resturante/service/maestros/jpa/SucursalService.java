package com.restaurante.resturante.service.maestros.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.mapper.maestros.SucursalDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.service.maestros.ISucursalService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SucursalService implements ISucursalService {
    private final SucursalRepository sucursalRepository;
    private final EmpresaRepository empresaRepository; // Necesario para vincular
    private final SucursalDtoMapper sucursalMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findAll() {
        return sucursalRepository.findAll().stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalDto findById(String id) {
        return sucursalRepository.findById(id)
                .map(sucursalMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public SucursalDto create(CreateSucursalDto dto) {
        // 1. Validar que la empresa exista
        Empresa empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new EntityNotFoundException("No se puede crear sucursal: Empresa no encontrada"));

        // 2. Convertir DTO a Entidad
        Sucursal sucursal = sucursalMapper.toEntity(dto);

        // 3. Establecer la relación manual si el mapper no lo hace
        sucursal.setEmpresa(empresa);
        sucursal.setEstado(true);

        // 4. Guardar y retornar
        Sucursal saved = sucursalRepository.save(sucursal);
        return sucursalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SucursalDto update(String id, CreateSucursalDto dto) {
        Sucursal existing = sucursalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        // Actualizar datos básicos
        existing.setNombre(dto.nombre());
        existing.setDireccion(dto.direccion());
        existing.setTelefono(dto.telefono());

        // Si permites cambiar de empresa (poco común, pero posible):
        if (!existing.getEmpresa().getId().equals(dto.empresaId())) {
            Empresa nuevaEmpresa = empresaRepository.findById(dto.empresaId())
                    .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
            existing.setEmpresa(nuevaEmpresa);
        }

        return sucursalMapper.toDto(sucursalRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        // Podrías implementar un borrado lógico aquí
        sucursalRepository.delete(sucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findByEmpresaId(String empresaId) {
        return sucursalRepository.findByEmpresaId(empresaId)
                .stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

}
