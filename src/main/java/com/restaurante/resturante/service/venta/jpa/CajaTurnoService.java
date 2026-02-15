package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.service.venta.ICajaTurnoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CajaTurnoService implements ICajaTurnoService {

        private final CajaTurnoRepository cajaRepository;
        private final PedidoRepository pedidoRepository; // Para el arqueo
        private final MovimientoCajaRepository movimientoRepository; // Para ingresos/egresos manuales
        private final CajaTurnoDtoMapper mapper;
        private final UserRepository userRepository;
        private final SucursalRepository sucursalRepository;

        @Override
        @Transactional(readOnly = true)
        public Optional<CajaTurnoDto> obtenerCajaActiva(String sucursalId, String userId) {
                return cajaRepository.findByUserIdAndSucursalIdAndEstado(userId, sucursalId, "ABIERTA")
                                .map(mapper::toDto);
        }

        @Override
        public CajaTurnoDto abrirCaja(AbrirCajaDto dto) {
                if (cajaRepository.existsBySucursalIdAndEstado(dto.sucursalId(), "ABIERTA")) {
                        throw new RuntimeException("YA EXISTE UNA CAJA ABIERTA EN ESTA SUCURSAL");
                }

                var user = userRepository.findById(dto.usuarioId())
                                .orElseThrow(() -> new RuntimeException("USUARIO NO ENCONTRADO"));
                var sucursal = sucursalRepository.findById(dto.sucursalId())
                                .orElseThrow(() -> new RuntimeException("SUCURSAL NO ENCONTRADA"));

                CajaTurno caja = CajaTurno.builder()
                                .montoApertura(dto.montoApertura())
                                .fechaApertura(LocalDateTime.now())
                                .estado("ABIERTA")
                                .user(user)
                                .sucursal(sucursal)
                                .active(true)
                                .build();

                return mapper.toDto(cajaRepository.save(caja));
        }

        @Override
        @Transactional(readOnly = true)
        public CajaResumentDto obtenerResumenArqueo(String cajaId) {
                CajaTurno caja = cajaRepository.findById(cajaId)
                                .orElseThrow(() -> new RuntimeException("CAJA NO ENCONTRADA"));

                // 1. Ventas
                BigDecimal efectivo = pedidoRepository.sumTotalByCajaAndMetodo(cajaId, "EFECTIVO");
                BigDecimal tarjeta = pedidoRepository.sumTotalByCajaAndMetodo(cajaId, "TARJETA");

                // 2. Movimientos Manuales
                BigDecimal ingresos = movimientoRepository.sumarPorTipoYTurno(cajaId,
                                com.restaurante.resturante.domain.ventas.TipoMovimiento.INGRESO);
                BigDecimal egresos = movimientoRepository.sumarPorTipoYTurno(cajaId,
                                com.restaurante.resturante.domain.ventas.TipoMovimiento.EGRESO);

                // 3. Mapper -> Incluye (Apertura + VentasEfectivo + Ingresos - Egresos)
                return mapper.toResumenDto(
                                caja,
                                efectivo != null ? efectivo : BigDecimal.ZERO,
                                tarjeta != null ? tarjeta : BigDecimal.ZERO,
                                ingresos != null ? ingresos : BigDecimal.ZERO,
                                egresos != null ? egresos : BigDecimal.ZERO);
        }

        @Override
        public void cerrarCaja(CerrarCajaDto dto) {
                CajaTurno caja = cajaRepository.findById(dto.id())
                                .orElseThrow(() -> new RuntimeException("CAJA NO ENCONTRADA"));

                // 1. Obtenemos el resumen actual para guardar el "Esperado" al momento del
                // cierre
                CajaResumentDto resumen = obtenerResumenArqueo(caja.getId());

                caja.setMontoCierreEsperado(resumen.totalEsperado());
                caja.setMontoCierreReal(dto.montoCierreReal());
                caja.setDiferencia(dto.montoCierreReal().subtract(resumen.totalEsperado()));
                caja.setFechaCierre(LocalDateTime.now());
                caja.setEstado("CERRADA");

                cajaRepository.save(caja);
        }
}
