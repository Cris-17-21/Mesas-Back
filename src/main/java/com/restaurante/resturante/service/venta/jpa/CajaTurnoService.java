package com.restaurante.resturante.service.venta.jpa;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;
import com.restaurante.resturante.mapper.venta.CajaTurnoDtoMapper;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.service.venta.ICajaTurnoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CajaTurnoService implements ICajaTurnoService {

    private final CajaTurnoRepository repository;
    private final UserRepository userRepository;
    private final SucursalRepository sucursalRepository;
    private final CajaTurnoDtoMapper mapper;

    @Override
    @Transactional
    public CajaTurnoDto abrirTurno(AbrirCajaDto dto) {
        // 1. Regla de Oro: Solo un turno abierto por usuario en esta sucursal
        if (repository.existsByUserIdAndSucursalIdAndEstado(dto.usuarioId(), dto.sucursalId(), "ABIERTA")) {
            throw new RuntimeException("Ya tienes un turno abierto en esta sucursal.");
        }

        // 2. Validar que existan las entidades
        var usuario = userRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        var sucursal = sucursalRepository.findById(dto.sucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        // 3. Crear el turno
        CajaTurno turno = CajaTurno.builder()
                .user(usuario)
                .sucursal(sucursal)
                .montoApertura(dto.montoApertura())
                .fechaApertura(LocalDateTime.now())
                .estado("ABIERTA")
                .active(true)
                .build();

        return mapper.toDto(repository.save(turno));
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResumentDto obtenerResumenActual(String turnoId) {
        CajaTurno turno = repository.findById(turnoId)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        // TODO: Aquí llamaremos a PedidoRepository para sumar ventas por medio de pago
        // Por ahora enviamos ceros para que compile el Mapper
        java.math.BigDecimal efectivo = java.math.BigDecimal.ZERO;
        java.math.BigDecimal tarjetas = java.math.BigDecimal.ZERO;
        java.math.BigDecimal otros = java.math.BigDecimal.ZERO;

        return mapper.toResumenDto(turno, efectivo, tarjetas, otros);
    }

    @Override
    @Transactional
    public CajaTurnoDto cerrarTurno(CerrarCajaDto dto) {
        CajaTurno turno = repository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        if (!"ABIERTA".equals(turno.getEstado())) {
            throw new RuntimeException("Este turno ya se encuentra cerrado.");
        }

        // TODO: Calcular diferencia contra el monto esperado antes de guardar
        turno.setEstado("CERRADA");
        turno.setFechaCierre(LocalDateTime.now());
        turno.setMontoCierre(dto.montoCierreReal());

        return mapper.toDto(repository.save(turno));
    }

    @Override
    @Transactional(readOnly = true)
    public CajaTurnoDto getTurnoActivo(String usuarioId, String sucursalId) {
        return repository.findByUserIdAndSucursalIdAndEstado(usuarioId, sucursalId, "ABIERTA")
                .map(mapper::toDto)
                .orElse(null); // El front sabrá que si es null, debe pedir apertura
    }
}
