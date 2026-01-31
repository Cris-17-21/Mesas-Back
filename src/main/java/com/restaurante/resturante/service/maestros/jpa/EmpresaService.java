package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.mapper.maestros.EmpresaDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.service.maestros.IEmpresaService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService implements IEmpresaService{
    
    private final EmpresaRepository empresaRepository;
    private final EmpresaDtoMapper empresaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaDto> findAll() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaDto findById(String id) {
        return empresaRepository.findById(id)
                .map(empresaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public EmpresaDto create(CreateEmpresaDto dto) {
        // 1. Validar si el RUC ya existe
        if (empresaRepository.existsByRuc(dto.ruc())) {
            throw new IllegalStateException("Ya existe una empresa registrada con el RUC: " + dto.ruc());
        }

        // 2. Convertir DTO a Entidad
        Empresa empresa = empresaMapper.toEntity(dto);
        
        // 3. Guardar empresa
        Empresa saved = empresaRepository.save(empresa);

        return empresaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public EmpresaDto update(String id, CreateEmpresaDto dto) {
        Empresa existing = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        // Validar RUC si ha cambiado
        if (!existing.getRuc().equals(dto.ruc()) && empresaRepository.existsByRuc(dto.ruc())) {
            throw new IllegalStateException("El RUC " + dto.ruc() + " ya está en uso por otra empresa");
        }

        // Actualizar campos
        existing.setRuc(dto.ruc());
        existing.setRazonSocial(dto.razonSocial());
        existing.setDireccionFiscal(dto.direccionFiscal());
        existing.setTelefono(dto.telefono());
        existing.setEmail(dto.email());
        existing.setLogoUrl(dto.logoUrl());
        // La fecha de afiliación usualmente no se debería cambiar, pero si lo requieres:
        // existing.setFechaAfiliacion(LocalDate.parse(dto.fechaAfiliacion()));

        return empresaMapper.toDto(empresaRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!empresaRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar: Empresa no encontrada");
        }
        empresaRepository.deleteById(id);
    }
}
