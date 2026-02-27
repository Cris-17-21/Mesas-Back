package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public List<EmpresaDto> findAllActive() {
        return empresaRepository.findAllByActiveTrue().stream()
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
        if (dto.ruc() == null) {
            throw new IllegalArgumentException("El RUC no puede ser nulo");
        }

        Optional<Empresa> empresaOptional = empresaRepository.findByRuc(dto.ruc());

        if (empresaOptional.isPresent()) {
            Empresa existing = empresaOptional.get();

            if (existing.getActive() == true) {
                throw new IllegalStateException("El RUC " + dto.ruc() + " ya está registrado.");
            } else {
                empresaMapper.updateEntityFromDto(dto, existing);
                Empresa reactivated = empresaRepository.save(existing);
                return empresaMapper.toDto(reactivated);
            }
        }

        Empresa empresa = empresaMapper.toEntity(dto);
        Empresa saved = empresaRepository.save(empresa);
        return empresaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public EmpresaDto update(String id, CreateEmpresaDto dto) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");

        Empresa existing = findExistingEmpresa(idSeguro);

        if (dto.ruc() != null && !existing.getRuc().equals(dto.ruc())) {
            Optional<Empresa> rucEnUso = empresaRepository.findByRuc(dto.ruc());
            if (rucEnUso.isPresent() && rucEnUso.get().getActive()) {
                throw new IllegalStateException(
                        "El nuevo RUC " + dto.ruc() + " ya está registrado en otra empresa activa.");
            } else {
                empresaMapper.updateEntityFromDto(dto, existing);
                existing.setActive(true); // Revivimos la empresa
                Empresa reactivated = empresaRepository.save(existing);
                return empresaMapper.toDto(reactivated);
            }
        }

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
        Empresa empresa = findExistingEmpresa(idSeguro);
        empresa.setActive(false);
        empresaRepository.save(empresa);
    }

    // -------- MÉTODOS AUXILIARES --------

    private Empresa findExistingEmpresa(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }
}
