package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;
import com.restaurante.resturante.mapper.maestros.MesaMapper;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.service.maestros.IMesaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MesaService implements IMesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    @Override
    public List<MesaResponseDto> listarTodas() {
        // Usando tu variable 'active' de la entidad
        return mesaRepository.findAll().stream()
                .map(mesaMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public MesaResponseDto cambiarEstado(String id, String nuevoEstado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no existe"));
        
        mesa.setEstado(nuevoEstado);
        return mesaMapper.toDTO(mesaRepository.save(mesa));
    }

    @Override
    public List<MesaResponseDto> listarActivasPorPiso(String pisoId) {
        // Ojo: asegúrate que tu Repo tenga findByPisoId(String id)
        return mesaRepository.findByPisoId(pisoId).stream()
                .filter(Mesa::getActive)
                .map(mesaMapper::toDTO)
                .toList();
    }

    @Override
    public void unirMesas(String idPrincipal, List<String> idsSecundarios) {
        Mesa principal = mesaRepository.findById(idPrincipal)
                .orElseThrow(() -> new RuntimeException("Mesa principal no encontrada"));

        List<Mesa> secundarias = mesaRepository.findAllById(idsSecundarios);
        secundarias.forEach(s -> {
            s.setPrincipal(principal);
            s.setEstado("UNIDA");
        });
        
        mesaRepository.saveAll(secundarias);
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDto obtenerPorId(String id) {
        // 1. Buscamos la mesa por su ID (String)
        // Usamos orElseThrow para manejar el caso de que el UUID no exista
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con el ID: " + id));

        // 2. Convertimos la entidad encontrada a DTO usando el mapper manual
        return mesaMapper.toDTO(mesa);
    }
}
