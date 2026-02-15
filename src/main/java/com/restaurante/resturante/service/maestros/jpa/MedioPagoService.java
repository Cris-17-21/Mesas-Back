package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.dto.maestro.CreateMedioPagoDto;
import com.restaurante.resturante.dto.maestro.MedioPagoDto;
import com.restaurante.resturante.mapper.maestros.MedioPagoMapper;
import com.restaurante.resturante.repository.maestro.MedioPagoRepository;
import com.restaurante.resturante.service.maestros.IMedioPagoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedioPagoService implements IMedioPagoService{

    private final MedioPagoRepository repository;
    private final MedioPagoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MedioPagoDto> listar(String empresaId) {
        return repository.findByEmpresaIdAndIsActiveTrue(empresaId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedioPagoDto crear(CreateMedioPagoDto dto) {
        // 1. Validar que no exista el nombre en ESA empresa específica
        if (repository.existsByNombreAndEmpresaIdAndIsActiveTrue(dto.nombre(), dto.empresaId())) {
            throw new RuntimeException("El medio de pago '" + dto.nombre() + "' ya existe en esta empresa.");
        }

        // 2. Convertir
        MedioPago entity = mapper.toEntity(dto);
        
        // 3. Asegurar estado activo
        entity.setActive(true);
        
        // 4. Lógica automática SUNAT (Si el front no envía código)
        if (dto.codigoSunat() == null || dto.codigoSunat().isBlank()) {
            // Regla simple: Efectivo='009', Otros='001'
            entity.setCodigoSunat(dto.esEfectivo() ? "009" : "001");
        }

        // 5. Guardar
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public MedioPagoDto actualizar(String id, CreateMedioPagoDto dto) {
        // Buscamos por ID y EMPRESA para asegurar que no editen datos de otro cliente
        MedioPago entity = repository.findByIdAndEmpresaIdAndIsActiveTrue(id, dto.empresaId())
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado o no pertenece a la empresa indicada"));

        // Actualizamos campos
        mapper.updateEntityFromDto(dto, entity);

        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(String id, String empresaId) {
        MedioPago entity = repository.findByIdAndEmpresaIdAndIsActiveTrue(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));
        
        // Soft Delete
        entity.setActive(false);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MedioPagoDto obtenerPorId(String id, String empresaId) {
        return repository.findByIdAndEmpresaIdAndIsActiveTrue(id, empresaId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));
    }
}
