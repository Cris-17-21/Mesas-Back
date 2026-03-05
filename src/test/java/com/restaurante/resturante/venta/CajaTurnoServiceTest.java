package com.restaurante.resturante.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.mapper.venta.CajaTurnoDtoMapper;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.service.venta.jpa.CajaTurnoService;

@ExtendWith(MockitoExtension.class)
class CajaTurnoServiceTest {

    @Mock
    private CajaTurnoRepository cajaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private MovimientoCajaRepository movimientoRepository;

    @Mock
    private CajaTurnoDtoMapper mapper;

    @InjectMocks
    private CajaTurnoService cajaService;

    @Test
    @DisplayName("Debería calcular el arqueo separando efectivo de virtual correctamente")
    void obtenerResumenArqueo_Success() {
        // GIVEN
        String cajaId = "caja-123";
        CajaTurno caja = CajaTurno.builder()
                .id(cajaId)
                .montoApertura(new BigDecimal("100.00"))
                .user(User.builder().username("cajero1").build())
                .estado("ABIERTA")
                .fechaApertura(LocalDateTime.now())
                .build();

        when(cajaRepository.findById(cajaId)).thenReturn(Optional.of(caja));

        // Ventas: 50 efectivo, 30 virtual
        when(pedidoRepository.sumTotalByCajaAndEsEfectivo(cajaId, true)).thenReturn(new BigDecimal("50.00"));
        when(pedidoRepository.sumTotalByCajaAndEsEfectivo(cajaId, false)).thenReturn(new BigDecimal("30.00"));

        // Movimientos: 20 ingreso, 10 egreso
        when(movimientoRepository.sumarPorTipoYTurno(eq(cajaId), eq(TipoMovimiento.INGRESO)))
                .thenReturn(new BigDecimal("20.00"));
        when(movimientoRepository.sumarPorTipoYTurno(eq(cajaId), eq(TipoMovimiento.EGRESO)))
                .thenReturn(new BigDecimal("10.00"));

        // Mock del mapper (Simula el comportamiento real para validar la lógica del
        // service)
        when(mapper.toResumenDto(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            CajaTurno c = invocation.getArgument(0);
            BigDecimal ef = invocation.getArgument(1);
            BigDecimal vi = invocation.getArgument(2);
            BigDecimal ing = invocation.getArgument(3);
            BigDecimal egr = invocation.getArgument(4);

            BigDecimal esperado = c.getMontoApertura().add(ef).add(ing).subtract(egr);

            return new CajaResumentDto(
                    c.getId(), c.getEstado(), c.getFechaApertura(), null, "cajero1",
                    c.getMontoApertura(), ing, egr,
                    ef, vi, BigDecimal.ZERO, ef.add(vi),
                    esperado, BigDecimal.ZERO, esperado.negate());
        });

        // WHEN
        CajaResumentDto result = cajaService.obtenerResumenArqueo(cajaId);

        // THEN
        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.totalVentasEfectivo());
        assertEquals(new BigDecimal("30.00"), result.totalVentasTarjeta());
        // Saldo esperado en caja (Caja Física) = 100 (Apertura) + 50 (Ventas Ef) + 20
        // (Ingresos) - 10 (Egresos) = 160
        assertEquals(new BigDecimal("160.00"), result.saldoEsperadoEnCaja());
    }
}
