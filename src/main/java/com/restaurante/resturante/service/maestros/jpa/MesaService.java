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
    @Transactional(readOnly = true)
    public List<MesaResponseDto> findByPisoAndActiveTrue(String pisoId) {
        return mesaMapper.toDTOList(mesaRepository.findByPisoIdAndActiveTrue(pisoId));
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

        if ("OCUPADA".equals(mesa.getEstado()) || "RESERVADA".equals(mesa.getEstado())) {
            throw new RuntimeException("No se puede eliminar una mesa que está " + mesa.getEstado());
        }
        mesa.setActive(false);
        mesaRepository.save(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDto cambiarEstado(String id, String nuevoEstado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        String actual = mesa.getEstado().toUpperCase();
        String destino = nuevoEstado.toUpperCase();

        if (!esTransicionValida(actual, destino)) {
            throw new IllegalArgumentException("TRANSICIÓN DE ESTADO NO VÁLIDA: DE " + actual + " A " + destino);
        }

        // El estado siempre debe persistirse en MAYÚSCULAS
        mesa.setEstado(destino);
        return mesaMapper.toDto(mesaRepository.save(mesa));
    }

    private boolean esTransicionValida(String estadoActual, String estadoNuevo) {
        if (estadoActual.equals(estadoNuevo)) {
            return true;
        }
        
        switch (estadoActual) {
            case "LIBRE":
                return "OCUPADA".equals(estadoNuevo) || "OCUPADA_UNION".equals(estadoNuevo) || "RESERVADA".equals(estadoNuevo);
            case "RESERVADA":
                return "OCUPADA".equals(estadoNuevo) || "LIBRE".equals(estadoNuevo);
            case "OCUPADA":
                return "PEDIENDO".equals(estadoNuevo) || "PRE_CUENTA".equals(estadoNuevo) || "LIBRE".equals(estadoNuevo);
            case "PEDIENDO":
            case "ATENDIENDO":
                return "PRE_CUENTA".equals(estadoNuevo) || "OCUPADA".equals(estadoNuevo) || "LIBRE".equals(estadoNuevo);
            case "PRE_CUENTA":
                return "PAGADA".equals(estadoNuevo) || "OCUPADA".equals(estadoNuevo) || "PEDIENDO".equals(estadoNuevo);
            case "PAGADA":
                return "SUCIA".equals(estadoNuevo) || "LIBRE".equals(estadoNuevo);
            case "SUCIA":
            case "LIMPIEZA":
                return "LIBRE".equals(estadoNuevo);
            case "OCUPADA_UNION":
                return "LIBRE".equals(estadoNuevo);
            default:
                return true;
        }
    }

    @Override
    @Transactional
    public void unirMesas(String idPrincipal, List<String> idsSecundarios) {
        Mesa principal = mesaRepository.findById(idPrincipal)
                .orElseThrow(() -> new RuntimeException("MESA PRINCIPAL NO ENCONTRADA"));

        if (principal.getPrincipal() != null) {
            throw new RuntimeException("LA MESA PRINCIPAL NO PUEDE SER UNA MESA YA UNIDA A OTRA");
        }

        List<Mesa> secundarias = mesaRepository.findAllById(idsSecundarios);

        for (Mesa secundaria : secundarias) {
            if (secondaryIsPrincipal(secundaria, idPrincipal))
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

    private boolean secondaryIsPrincipal(Mesa sec, String pId) {
        return sec.getId().equals(pId);
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
