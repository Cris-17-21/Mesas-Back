package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;
import java.util.Objects;

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
public class EmpresaService implements IEmpresaService {

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
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        return empresaRepository.findById(idSeguro)
                .map(empresaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public EmpresaDto create(CreateEmpresaDto dto) {

        // Validar RUC
        validarRucUnico(dto.ruc());
        Empresa empresa = empresaMapper.toEntity(dto);

        // Normalizar la Razón Social en mayúscula
        empresa.setRazonSocial(empresa.getRazonSocial());

        // Guardar empresa
        Empresa saved = empresaRepository.save(empresa);
        return empresaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public EmpresaDto update(String id, CreateEmpresaDto dto) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");

        Empresa existing = empresaRepository.findById(idSeguro)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        // Validar RUC si ha cambiado
        if (dto.ruc() != null && !existing.getRuc().equals(dto.ruc())) {
            validarRucUnico(dto.ruc());
        }

        // Actualizar campos
        empresaMapper.updateEntityFromDto(dto, existing);

        return empresaMapper.toDto(empresaRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        if (!empresaRepository.existsById(idSeguro)) {
            throw new EntityNotFoundException("No se puede eliminar: Empresa no encontrada");
        }
        empresaRepository.deleteById(idSeguro);
    }

    // -------- MÉTODOS AUXILIARES --------
    private void validarRucUnico(String ruc) {
        if (ruc == null) {
            throw new IllegalArgumentException("El RUC no puede ser nulo");
        }
        if (empresaRepository.existsByRuc(ruc)) {
            throw new IllegalStateException("El RUC " + ruc + " ya está registrado.");
        }
    }
}
