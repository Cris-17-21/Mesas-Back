package com.restaurante.resturante.service.maestros.jpa;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.mapper.maestros.EmpresaDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.MedioPagoRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionAuthService;
import com.restaurante.resturante.service.api_facturacion.FacturacionEmpresaService.FacturacionEmpresaService;
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
    private final FacturacionAuthService facturacionAuthService;
    private final FacturacionEmpresaService facturacionEmpresaService;
    private final MedioPagoRepository medioPagoRepository;
    private RestClient restClient;

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
        seedDefaultPaymentMethods(saved);
        syncWithApi(saved);
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
                syncUpdateWithApi(reactivated);
                return empresaMapper.toDto(reactivated);
            }
        }

        empresaMapper.updateEntityFromDto(dto, existing);
        Empresa saved = empresaRepository.save(existing);
        syncUpdateWithApi(saved);
        return empresaMapper.toDto(saved);
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

    @Override
    @Transactional
    public EmpresaDto uploadLogo(String id, MultipartFile file) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Empresa empresa = findExistingEmpresa(idSeguro);
        try {
            empresa.setLogoUrl(file.getBytes());
            Empresa saved = empresaRepository.save(empresa);
            try {
                facturacionEmpresaService.actualizarLogo(saved, file);
            } catch (Exception e) {
                // No bloquear si falla sincronización
            }
            return empresaMapper.toDto(saved);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de logo", e);
        }
    }

    @Override
    @Transactional
    public EmpresaDto uploadCertificado(String id, MultipartFile file) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Empresa empresa = findExistingEmpresa(idSeguro);
        try {
            String base64Cert = Base64.getEncoder().encodeToString(file.getBytes());
            empresa.setCertificadoDigital(base64Cert);
            Empresa saved = empresaRepository.save(empresa);
            try {
                facturacionEmpresaService.actualizarCertificado(saved, file, saved.getClaveCertificado());
            } catch (Exception e) {
                // No bloquear si falla sincronización
            }
            return empresaMapper.toDto(saved);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de certificado", e);
        }
    }

    // -------- MÉTODOS AUXILIARES --------

    private void seedDefaultPaymentMethods(Empresa empresa) {
        List.of(
            MedioPago.builder().nombre("EFECTIVO").esEfectivo(true).empresa(empresa).build(),
            MedioPago.builder().nombre("YAPE").esEfectivo(false).empresa(empresa).build(),
            MedioPago.builder().nombre("PLIN").esEfectivo(false).empresa(empresa).build(),
            MedioPago.builder().nombre("TARJETA").esEfectivo(false).empresa(empresa).build()
        ).forEach(medioPagoRepository::save);
    }

    private void syncWithApi(Empresa empresa) {
        try {
            facturacionEmpresaService.crearEmpresa(empresa);
        } catch (Exception e) {
            // No bloquear la creación local si falla la sincronización con la API
        }
    }

    private void syncUpdateWithApi(Empresa empresa) {
        try {
            facturacionEmpresaService.actualizarEmpresa(empresa);
        } catch (Exception e) {
            // No bloquear la actualización local si falla la sincronización con la API
        }
    }

    private Empresa findExistingEmpresa(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }
}
