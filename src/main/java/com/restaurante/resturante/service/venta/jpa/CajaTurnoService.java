package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;
import com.restaurante.resturante.mapper.venta.CajaTurnoDtoMapper;
import com.restaurante.resturante.repository.maestro.MesaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
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
        private final MesaRepository mesaRepository;

        @Override
        @Transactional(readOnly = true)
        public Optional<CajaTurnoDto> obtenerCajaActiva(String sucursalId, String userId) {
                // Ahora buscamos cualquier caja abierta en la sucursal, sin importar el usuario
                return cajaRepository.findBySucursalIdAndEstado(sucursalId, "ABIERTA")
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

                String codigoApertura = "AP-"
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                                + "-" + sucursal.getId().substring(0, 4).toUpperCase();

                BigDecimal cashApertura = dto.montoAperturaEfectivo() != null ? dto.montoAperturaEfectivo() : dto.montoApertura();
                BigDecimal cardApertura = dto.montoAperturaVirtual() != null ? dto.montoAperturaVirtual() : BigDecimal.ZERO;
                BigDecimal totalApertura = dto.montoApertura() != null ? dto.montoApertura() : cashApertura.add(cardApertura);

                CajaTurno caja = CajaTurno.builder()
                                .codigoApertura(codigoApertura)
                                .montoApertura(totalApertura)
                                .montoAperturaEfectivo(cashApertura)
                                .montoAperturaVirtual(cardApertura)
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
                BigDecimal efectivo = pedidoRepository.sumTotalByCajaAndEsEfectivo(cajaId, true);
                BigDecimal virtual = pedidoRepository.sumTotalByCajaAndEsEfectivo(cajaId, false);

                // 2. Movimientos Manuales Desglosados
                BigDecimal ingresosEf = movimientoRepository.sumarPorTipoTurnoyEsEfectivo(cajaId, TipoMovimiento.INGRESO, true);
                BigDecimal egresosEf = movimientoRepository.sumarPorTipoTurnoyEsEfectivo(cajaId, TipoMovimiento.EGRESO, true);
                BigDecimal ingresosVir = movimientoRepository.sumarPorTipoTurnoyEsEfectivo(cajaId, TipoMovimiento.INGRESO, false);
                BigDecimal egresosVir = movimientoRepository.sumarPorTipoTurnoyEsEfectivo(cajaId, TipoMovimiento.EGRESO, false);

                // 3. Mapper -> Incluye desglosado
                return mapper.toResumenDto(
                                caja,
                                efectivo != null ? efectivo : BigDecimal.ZERO,
                                virtual != null ? virtual : BigDecimal.ZERO,
                                ingresosEf != null ? ingresosEf : BigDecimal.ZERO,
                                egresosEf != null ? egresosEf : BigDecimal.ZERO,
                                ingresosVir != null ? ingresosVir : BigDecimal.ZERO,
                                egresosVir != null ? egresosVir : BigDecimal.ZERO);
        }

        @Override
        public void cerrarCaja(CerrarCajaDto dto) {
                CajaTurno caja = cajaRepository.findById(dto.id())
                                .orElseThrow(() -> new RuntimeException("CAJA NO ENCONTRADA"));

                String sucursalId = caja.getSucursal().getId();

                // 1. Validar que no haya pedidos abiertos en la sucursal
                boolean tienePedidosAbiertos = !pedidoRepository.findBySucursalIdAndEstado(sucursalId, "ABIERTO").isEmpty();
                if (tienePedidosAbiertos) {
                        throw new RuntimeException("NO SE PUEDE CERRAR LA CAJA: EXISTEN PEDIDOS ABIERTOS (SIN FINALIZAR) EN LA SUCURSAL");
                }

                // 2. Validar que no haya mesas ocupadas o unidas en el salón
                long mesasOcupadas = mesaRepository.countOccupiedTablesBySucursal(sucursalId);
                if (mesasOcupadas > 0) {
                        throw new RuntimeException("NO SE PUEDE CERRAR LA CAJA: EXISTEN MESAS OCUPADAS O UNIDAS EN EL SALÓN");
                }

                // 3. Obtenemos el resumen actual para guardar el "Esperado" al momento del cierre
                CajaResumentDto resumen = obtenerResumenArqueo(caja.getId());

                BigDecimal cashEsperado = resumen.saldoEsperadoEnCaja();
                BigDecimal cardEsperado = resumen.saldoEsperadoVirtual();
                BigDecimal overallEsperado = cashEsperado.add(cardEsperado);

                BigDecimal cashReal = dto.efectivoCierreReal() != null ? dto.efectivoCierreReal() : BigDecimal.ZERO;
                BigDecimal cardReal = dto.virtualCierreReal() != null ? dto.virtualCierreReal() : BigDecimal.ZERO;
                BigDecimal overallReal = cashReal.add(cardReal);

                BigDecimal diff = overallReal.subtract(overallEsperado);

                if (diff.compareTo(BigDecimal.ZERO) != 0 && (dto.comentario() == null || dto.comentario().trim().isEmpty())) {
                        throw new IllegalArgumentException("DEBE INGRESAR UNA OBSERVACIÓN/JUSTIFICACIÓN POR LA DIFERENCIA EN EL ARQUEO DE CAJA");
                }

                caja.setMontoCierreEsperado(overallEsperado);
                caja.setMontoCierreEsperadoEfectivo(cashEsperado);
                caja.setMontoCierreEsperadoVirtual(cardEsperado);
                caja.setMontoCierreReal(overallReal);
                caja.setMontoCierreRealEfectivo(cashReal);
                caja.setMontoCierreRealVirtual(cardReal);
                caja.setDiferencia(diff);
                caja.setObservaciones(dto.comentario());
                caja.setFechaCierre(LocalDateTime.now());
                caja.setEstado("CERRADA");

                cajaRepository.save(caja);
        }

        @Override
        @Transactional(readOnly = true)
        public java.util.List<CajaTurnoDto> obtenerHistorial(String sucursalId) {
                return cajaRepository.findBySucursalIdAndEstadoOrderByFechaAperturaDesc(sucursalId, "CERRADA")
                                .stream()
                                .map(mapper::toDto)
                                .toList();
        }
}
