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
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.mapper.maestros.SucursalDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.maestros.ISucursalService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SucursalService implements ISucursalService {
    private final SucursalRepository sucursalRepository;
    private final EmpresaRepository empresaRepository;
    private final SucursalDtoMapper sucursalMapper;
    private final UserAccessRepository userAccessRepository;
    private final UserRepository userRepository;

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
        // 1. Validar que la empresa exista y esté activa
        Empresa empresa = findExistingEmpresa(dto.empresaId());
        if (!empresa.getActive()) {
            throw new IllegalStateException("No se puede crear una sucursal para una empresa inactiva.");
        }

        // 2. Buscar si ya existe una sucursal con ese nombre en esa misma empresa
        Optional<Sucursal> sucursalExistente = sucursalRepository
                .findByNombreIgnoreCaseAndEmpresaId(dto.nombre(), dto.empresaId());

        if (sucursalExistente.isPresent()) {
            Sucursal existing = sucursalExistente.get();
            if (existing.getEstado()) {
                throw new IllegalStateException("Ya existe una sucursal con ese nombre en esta misma empresa.");
            } else {
                sucursalMapper.updateEntity(dto, existing);
                existing.setEstado(true);
                Sucursal reactivated = sucursalRepository.save(existing);
                return sucursalMapper.toDto(reactivated);
            }
        }

        // 3. Creación nueva
        Sucursal sucursal = sucursalMapper.toEntity(dto);
        sucursal.setEmpresa(empresa);
        sucursal.setEstado(true);
        Sucursal saved = sucursalRepository.save(sucursal);
        return sucursalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SucursalDto update(String id, CreateSucursalDto dto) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Sucursal existing = findExistingSucursal(idSeguro);

        // Validar cambio de nombre (para evitar duplicados en la misma empresa)
        if (dto.nombre() != null && !existing.getNombre().equalsIgnoreCase(dto.nombre())) {
            Optional<Sucursal> nombreEnUso = sucursalRepository.findByNombreIgnoreCaseAndEmpresaId(
                    dto.nombre(),
                    dto.empresaId());
            if (nombreEnUso.isPresent() && nombreEnUso.get().getEstado()) {
                throw new IllegalStateException(
                        "El nombre '" + dto.nombre() + "' ya está en uso por otra sucursal activa de esta empresa.");
            } else if (nombreEnUso.isPresent() && !nombreEnUso.get().getEstado()) {
                List<UserAccess> accesos = userAccessRepository.findBySucursalId(existing.getId());
                accesos.forEach(acceso -> {
                    acceso.setActive(true);
                    User user = acceso.getUser();
                    user.setActive(true); // Lo despertamos
                    userRepository.save(user);
                });
                userAccessRepository.saveAll(accesos);
                Sucursal inactiva = nombreEnUso.get();
                sucursalMapper.updateEntity(dto, inactiva);
                inactiva.setEstado(true);
            }
        }

        // Validar cambio de empresa (caso raro)
        if (dto.empresaId() != null && !existing.getEmpresa().getId().equals(dto.empresaId())) {
            Empresa nuevaEmpresa = findExistingEmpresa(dto.empresaId());
            if (!nuevaEmpresa.getActive()) {
                throw new IllegalStateException("No se puede transferir la sucursal a una empresa inactiva.");
            }
            existing.setEmpresa(nuevaEmpresa);
        }

        sucursalMapper.updateEntity(dto, existing);
        existing.setEstado(true); // Aseguramos que se mantenga activa al actualizar

        return sucursalMapper.toDto(sucursalRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Sucursal sucursal = findExistingSucursal(idSeguro);

        // 1. Apagamos la sucursal
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
