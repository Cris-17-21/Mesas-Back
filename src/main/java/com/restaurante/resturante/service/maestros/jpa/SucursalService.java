package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

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
    private final EmpresaRepository empresaRepository;
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
    public List<SucursalDto> findAllActive() {
        return sucursalRepository.findAllByEstadoTrue().stream()
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
        Empresa empresa = findExistingEmpresa(dto.empresaId());
        if (!empresa.getActive()) {
            throw new IllegalStateException("No se puede crear una sucursal para una empresa inactiva.");
        }

        // 2. Convertir DTO a Entidad
        Sucursal sucursal = sucursalMapper.toEntity(dto);

        // 3. Establecer la relación manual
        sucursal.setEmpresa(empresa);
        sucursal.setEstado(true);

        // 4. Guardar y retornar
        Sucursal saved = sucursalRepository.save(sucursal);
        return sucursalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SucursalDto update(String id, CreateSucursalDto dto) {
        Sucursal existing = findExistingSucursal(id);

        // Actualizar datos con el mapper
        sucursalMapper.updateEntity(dto, existing);

        // En caso se cambie de empresa - NOTA: Caso poco común
        if (!existing.getEmpresa().getId().equals(dto.empresaId())) {
            Empresa nuevaEmpresa = findExistingEmpresa(dto.empresaId());
            if (!nuevaEmpresa.getActive()) {
                throw new IllegalStateException("No se puede transferir la sucursal a una empresa inactiva.");
            }
            existing.setEmpresa(nuevaEmpresa);
        }

        return sucursalMapper.toDto(sucursalRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Sucursal sucursal = findExistingSucursal(id);
        sucursal.setEstado(false);
        sucursalRepository.save(sucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findSucursalesByEmpresaId(String empresaId) {
        return sucursalRepository.findByEmpresaIdAndEstadoTrue(empresaId)
                .stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

    // -------- MÉTODOS AUXILIARES --------
    private Sucursal findExistingSucursal(String id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
    }

    private Empresa findExistingEmpresa(String empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }
}
