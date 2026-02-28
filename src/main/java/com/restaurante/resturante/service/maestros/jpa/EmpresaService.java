package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.mapper.maestros.EmpresaDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.maestros.IEmpresaService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService implements IEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaDtoMapper empresaMapper;
    private final SucursalRepository sucursalRepository;
    private final UserAccessRepository userAccessRepository;
    private final UserRepository userRepository;

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
                List<UserAccess> accesos = userAccessRepository.findByEmpresaId(existing.getId());
                accesos.forEach(acceso -> {
                    acceso.setActive(true);
                    User user = acceso.getUser();
                    user.setActive(true); // Lo despertamos
                    userRepository.save(user);
                });
                userAccessRepository.saveAll(accesos);
                empresaMapper.updateEntityFromDto(dto, existing);
                existing.setActive(true);
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
                List<UserAccess> accesos = userAccessRepository.findByEmpresaId(existing.getId());
                accesos.forEach(acceso -> {
                    acceso.setActive(true);
                    User user = acceso.getUser();
                    user.setActive(true); // Lo despertamos
                    userRepository.save(user);
                });
                userAccessRepository.saveAll(accesos);
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

        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdAndEstadoTrue(empresa.getId());
        sucursales.forEach(sucursal -> {
            sucursal.setEstado(false);
        });
        sucursalRepository.saveAll(sucursales);

        List<UserAccess> accesos = userAccessRepository.findByEmpresaId(empresa.getId());

        accesos.forEach(acceso -> {
            acceso.setActive(false);
            User user = acceso.getUser();
            user.setActive(false);
            userRepository.save(user);
        });

        userAccessRepository.saveAll(accesos);
    }

    // -------- MÉTODOS AUXILIARES --------

    private Empresa findExistingEmpresa(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }
}
