package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.MovimientoCaja;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
import com.restaurante.resturante.dto.venta.MovimientoCajaDto;
import com.restaurante.resturante.dto.venta.MovimientoCajaResponseDto;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoCajaService {

    private final MovimientoCajaRepository movimientoRepository;
    private final CajaTurnoRepository cajaRepository;
    private final UserRepository userRepository;

    public MovimientoCajaResponseDto registrarMovimiento(MovimientoCajaDto dto) {
        // 1. Validar Caja
        CajaTurno caja = cajaRepository.findById(dto.cajaId())
                .orElseThrow(() -> new RuntimeException("CAJA NO ENCONTRADA"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("LA CAJA DEBE ESTAR ABIERTA PARA REGISTRAR MOVIMIENTOS");
        }

        // 2. Validar Usuario (quien hace el movimiento, puede ser distinto al cajero)
        var user = userRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("USUARIO NO ENCONTRADO"));

        // 3. Crear Movimiento
        MovimientoCaja movimiento = MovimientoCaja.builder()
                .cajaTurno(caja)
                .usuario(user)
                .tipo(TipoMovimiento.valueOf(dto.tipo())) // INGRESO, EGRESO
                .monto(dto.monto())
                .descripcion(dto.descripcion())
                .fecha(LocalDateTime.now())
                .build();

        movimiento = movimientoRepository.save(movimiento);

        return new MovimientoCajaResponseDto(
                movimiento.getId(),
                movimiento.getTipo().name(),
                movimiento.getMonto(),
                movimiento.getDescripcion(),
                movimiento.getFecha(),
                user.getUsername());
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaResponseDto> listarMovimientosPorCaja(String cajaId) {
        return movimientoRepository.findByCajaTurnoIdOrderByFechaDesc(cajaId).stream()
                .map(m -> new MovimientoCajaResponseDto(
                        m.getId(),
                        m.getTipo().name(),
                        m.getMonto(),
                        m.getDescripcion(),
                        m.getFecha(),
                        m.getUsuario().getUsername()))
                .toList();
    }
}
