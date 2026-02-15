package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.domain.maestros.Piso;
import com.restaurante.resturante.dto.maestro.CreateMesaDto;
import com.restaurante.resturante.dto.maestro.MesaResponseDto;
import com.restaurante.resturante.mapper.maestros.MesaMapper;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.repository.maestro.PisoRepository;
import com.restaurante.resturante.service.maestros.IMesaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MesaService implements IMesaService {

    private final MesaRepository mesaRepository;
    private final PisoRepository pisoRepository;
    private final MesaMapper mesaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDto> findByPiso(String pisoId) {
        return mesaMapper.toDTOList(mesaRepository.findByPisoId(pisoId));
    }

    @Override
    @Transactional
    public MesaResponseDto create(CreateMesaDto dto) {
        Piso piso = pisoRepository.findById(dto.pisoId())
                .orElseThrow(() -> new RuntimeException("Piso no encontrado"));

        Mesa mesa = mesaMapper.toEntity(dto);
        mesa.setPiso(piso);
        // Garantizamos que el código esté en MAYÚSCULAS
        mesa.setCodigoMesa(dto.codigoMesa().toUpperCase());

        return mesaMapper.toDto(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaResponseDto update(String id, CreateMesaDto dto) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        mesa.setCodigoMesa(dto.codigoMesa().toUpperCase());
        mesa.setCapacidad(dto.capacidad());
        mesa.setActive(dto.active());

        return mesaMapper.toDto(mesaRepository.save(mesa));
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDto obtenerPorId(String id) {
        return mesaRepository.findById(id)
                .map(mesaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
    }

    @Override
    @Transactional
    public void eliminar(String id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        mesa.setActive(false);
        mesaRepository.save(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDto cambiarEstado(String id, String nuevoEstado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // El estado siempre debe persistirse en MAYÚSCULAS
        mesa.setEstado(nuevoEstado.toUpperCase());
        return mesaMapper.toDto(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public void unirMesas(String idPrincipal, List<String> idsSecundarios) {
        Mesa principal = mesaRepository.findById(idPrincipal)
                .orElseThrow(() -> new RuntimeException("MESA PRINCIPAL NO ENCONTRADA"));

        // Validar que la principal esté ocupada (con pedido)
        if (!"OCUPADA".equals(principal.getEstado())) {
            throw new RuntimeException("LA MESA PRINCIPAL DEBE ESTAR OCUPADA PARA UNIR OTRAS");
        }

        List<Mesa> secundarias = mesaRepository.findAllById(idsSecundarios);

        for (Mesa secundaria : secundarias) {
            if (secundaria.getId().equals(idPrincipal))
                continue;

            // REGLA: Solo unir mesas que estén LIBRES
            if (!"LIBRE".equals(secundaria.getEstado())) {
                throw new RuntimeException("LA MESA " + secundaria.getCodigoMesa() + " NO ESTÁ LIBRE");
            }

            secundaria.setPrincipal(principal);
            secundaria.setEstado("OCUPADA_UNION");
        }

        mesaRepository.saveAll(secundarias);
    }

    @Override
    @Transactional
    public void separarMesas(String idPrincipal) {
        // Optimización: Usar el repositorio en lugar de traer todas las mesas a memoria
        List<Mesa> mesasUnidas = mesaRepository.findByPrincipalId(idPrincipal);

        mesasUnidas.forEach(m -> {
            m.setPrincipal(null);
            m.setEstado("LIBRE");
        });

        mesaRepository.saveAll(mesasUnidas);
    }
}
