package com.restaurante.resturante.service.compras.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.dto.compras.ProveedorDto;
import com.restaurante.resturante.mapper.compras.ProveedorDtoMapper;
import com.restaurante.resturante.repository.compras.ProveedorRepository;
import com.restaurante.resturante.service.compras.IProveedorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements IProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorDtoMapper proveedorMapper;
    private final com.restaurante.resturante.repository.compras.ProveedorMetodosPagoRepository metodosPagoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDto> findAll() {
        return proveedorRepository.findAll().stream()
                .map(proveedorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProveedorDto> findById(Integer id) {
        return proveedorRepository.findById(id)
                .map(proveedorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor findEntityById(Integer id) {
        return proveedorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ProveedorDto save(ProveedorDto dto) {
        Proveedor entity = proveedorMapper.toEntity(dto);
        Proveedor saved = proveedorRepository.save(entity);

        if (dto.metodosPago() != null && !dto.metodosPago().isEmpty()) {
            savePaymentMethods(saved.getIdProveedor(), dto.metodosPago());
        }

        // Fetch again to include payments in return (or manually construct DTO)
        return findById(saved.getIdProveedor()).orElse(proveedorMapper.toDto(saved));
    }

    @Override
    @Transactional
    public ProveedorDto update(Integer id, ProveedorDto dto) {
        return proveedorRepository.findById(id).map(existing -> {
            existing.setRazonSocial(dto.razonSocial());
            existing.setNombreComercial(dto.nombreComercial());
            existing.setRuc(dto.ruc());
            existing.setDireccion(dto.direccion());
            existing.setTelefono(dto.telefono());
            existing.setEstado(dto.estado());
            Proveedor saved = proveedorRepository.save(existing);

            // Update payment methods
            metodosPagoRepository.deleteByIdProveedor(id);
            if (dto.metodosPago() != null && !dto.metodosPago().isEmpty()) {
                savePaymentMethods(id, dto.metodosPago());
            }

            return findById(id).orElse(proveedorMapper.toDto(saved));
        }).orElse(null);
    }

    private void savePaymentMethods(Integer providerId,
            List<com.restaurante.resturante.dto.compras.ProveedorMetodoPagoDto> methods) {
        List<com.restaurante.resturante.domain.compras.ProveedorMetodosPago> entities = methods.stream()
                .map(m -> com.restaurante.resturante.domain.compras.ProveedorMetodosPago.builder()
                        .idProveedor(providerId)
                        .idTipoPago(m.idTipoPago())
                        .datosPago(m.datosPago())
                        .build())
                .collect(Collectors.toList());
        metodosPagoRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        proveedorRepository.deleteById(id);
    }
}
