package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Piso;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.maestro.CreatePisoDto;
import com.restaurante.resturante.dto.maestro.PisoDto;
import com.restaurante.resturante.mapper.maestros.PisoDtoMapper;
import com.restaurante.resturante.repository.maestro.PisoRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.service.maestros.IPisoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PisoService implements IPisoService {
    
    private final PisoRepository pisoRepository;
    private final SucursalRepository sucursalRepository;
    private final PisoDtoMapper pisoMapper;

    @Override
    public List<PisoDto> findAllBySucursal(String sucursalId) {
        // Necesitarás agregar findBySucursalId en tu PisoRepository
        return pisoRepository.findBySucursalIdAndActiveTrue(sucursalId)
                .stream()
                .map(pisoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PisoDto create(CreatePisoDto dto) {
        Sucursal sucursal = sucursalRepository.findById(dto.sucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        
        Piso piso = pisoMapper.toEntity(dto);
        piso.setSucursal(sucursal);
        
        return pisoMapper.toDto(pisoRepository.save(piso));
    }

    @Override
    public PisoDto findById(String id) {
        return pisoRepository.findById(id)
                .map(pisoMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Piso no encontrado"));
    }

    @Override
    @Transactional
    public PisoDto update(String id, CreatePisoDto dto) {
        Piso piso = pisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Piso no encontrado"));
        
        piso.setNombre(dto.nombre());
        piso.setDescripcion(dto.descripcion());
        
        return pisoMapper.toDto(pisoRepository.save(piso));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Piso piso = pisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Piso no encontrado"));
        piso.setActive(false); // Borrado lógico
        pisoRepository.save(piso);
    }
}
