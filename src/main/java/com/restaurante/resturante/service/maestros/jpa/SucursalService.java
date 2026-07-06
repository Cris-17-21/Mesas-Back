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
import com.restaurante.resturante.service.api_facturacion.FacturacionSucursalService;
import com.restaurante.resturante.service.maestros.ISucursalService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
public class SucursalService implements ISucursalService {
    private final SucursalRepository sucursalRepository;
    private final EmpresaRepository empresaRepository;
    private final SucursalDtoMapper sucursalMapper;
    private final UserAccessRepository userAccessRepository;
    private final UserRepository userRepository;
    private final FacturacionSucursalService facturacionSucursalService;

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findAll() {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId != null) {
                return sucursalRepository.findByEmpresaId(empresaId).stream()
                        .map(sucursalMapper::toDto)
                        .toList();
            }
        }
        return sucursalRepository.findAll().stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findAllActive() {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId != null) {
                return sucursalRepository.findByEmpresaIdAndEstadoTrueOrderByCreatedDateAsc(empresaId).stream()
                        .map(sucursalMapper::toDto)
                        .toList();
            }
        }
        return sucursalRepository.findAllByEstadoTrueOrderByCreatedDateAsc().stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalDto findById(String id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada con ID: " + id));
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId != null && !sucursal.getEmpresa().getId().equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para acceder a esta sucursal.");
            }
        }
        return sucursalMapper.toDto(sucursal);
    }

    @Override
    @Transactional
    public SucursalDto create(CreateSucursalDto dto) {
        String targetEmpresaId = dto.empresaId();
        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId == null) {
                throw new AccessDeniedException("No se encontró tu empresa.");
            }
            targetEmpresaId = empresaId;
        }

        // 1. Validar que la empresa exista y esté activa
        Empresa empresa = findExistingEmpresa(targetEmpresaId);
        if (!empresa.getActive()) {
            throw new IllegalStateException("No se puede crear una sucursal para una empresa inactiva.");
        }

        // 2. Buscar si ya existe una sucursal con ese nombre en esa misma empresa
        Optional<Sucursal> sucursalExistente = sucursalRepository
                .findByNombreIgnoreCaseAndEmpresaId(dto.nombre(), targetEmpresaId);

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
        syncWithApi(saved);
        return sucursalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SucursalDto update(String id, CreateSucursalDto dto) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Sucursal existing = findExistingSucursal(idSeguro);
        String targetEmpresaId = dto.empresaId();

        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId == null || !existing.getEmpresa().getId().equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para modificar esta sucursal.");
            }
            targetEmpresaId = empresaId;
        }

        // Validar cambio de nombre (para evitar duplicados en la misma empresa)
        if (dto.nombre() != null && !existing.getNombre().equalsIgnoreCase(dto.nombre())) {
            Optional<Sucursal> nombreEnUso = sucursalRepository.findByNombreIgnoreCaseAndEmpresaId(
                    dto.nombre(),
                    targetEmpresaId);
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
        if (targetEmpresaId != null && !existing.getEmpresa().getId().equals(targetEmpresaId)) {
            Empresa nuevaEmpresa = findExistingEmpresa(targetEmpresaId);
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

        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId == null || !sucursal.getEmpresa().getId().equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para eliminar esta sucursal.");
            }
        }

        // Validar si existen datos registrados asociados a esta sucursal
        long productos = sucursalRepository.countProductosBySucursalId(idSeguro);
        long pedidos = sucursalRepository.countPedidosBySucursalId(idSeguro);
        long compras = sucursalRepository.countPedidosCompraBySucursalId(idSeguro);
        long comprobantes = sucursalRepository.countComprobantesBySucursalId(idSeguro);
        long cajas = sucursalRepository.countCajasBySucursalId(idSeguro);
        long pisos = sucursalRepository.countPisosBySucursalId(idSeguro);

        if (productos > 0 || pedidos > 0 || compras > 0 || comprobantes > 0 || cajas > 0 || pisos > 0) {
            throw new IllegalStateException("No se puede eliminar la sucursal porque tiene datos registrados asociados (productos, pedidos, compras o movimientos de caja).");
        }

        // 1. Apagamos la sucursal
        sucursal.setEstado(false);
        sucursalRepository.save(sucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> findSucursalesByEmpresaId(String empresaId) {
        if (isAuthenticatedUserRestaurantAdmin()) {
            String adminEmpresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (adminEmpresaId == null || !adminEmpresaId.equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para consultar las sucursales de esta empresa.");
            }
        }
        return sucursalRepository.findByEmpresaIdAndEstadoTrueOrderByCreatedDateAsc(empresaId)
                .stream()
                .map(sucursalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SucursalDto toggleStatus(String id) {
        String idSeguro = Objects.requireNonNull(id, "El ID no puede ser nulo");
        Sucursal sucursal = findExistingSucursal(idSeguro);

        if (isAuthenticatedUserRestaurantAdmin()) {
            String empresaId = getAuthenticatedUserEmpresaIdOrNull();
            if (empresaId == null || !sucursal.getEmpresa().getId().equals(empresaId)) {
                throw new AccessDeniedException("No tienes permiso para modificar esta sucursal.");
            }
        }

        boolean newStatus = !sucursal.getEstado();

        if (newStatus && !sucursal.getEmpresa().getActive()) {
            throw new IllegalStateException("No se puede activar una sucursal si su empresa está inactiva.");
        }

        sucursal.setEstado(newStatus);
        Sucursal saved = sucursalRepository.save(sucursal);
        syncWithApi(saved);
        return sucursalMapper.toDto(saved);
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

    private void syncWithApi(Sucursal sucursal) {
        try {
            facturacionSucursalService.syncSucursal(sucursal);
        } catch (Exception e) {
            log.warn("No se pudo sincronizar sucursal con API: {}", e.getMessage());
        }
    }

    private boolean isAuthenticatedUserRestaurantAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_RESTAURANTE"));
    }

    private String getAuthenticatedUserEmpresaIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        List<UserAccess> accesses = userAccessRepository.findByUserUsername(username);
        return accesses.stream()
                .filter(UserAccess::getActive)
                .map(acc -> acc.getEmpresa().getId())
                .findFirst()
                .orElse(null);
    }
}
