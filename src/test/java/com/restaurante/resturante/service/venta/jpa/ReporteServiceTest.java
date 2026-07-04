package com.restaurante.resturante.service.venta.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.inventario.Inventario;
import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.MedioPago;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.MovimientoCaja;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.domain.ventas.PedidoPago;
import com.restaurante.resturante.dto.venta.ReportesDto.CajaAuditDto;
import com.restaurante.resturante.dto.venta.ReportesDto.MenuRankingDto;
import com.restaurante.resturante.dto.venta.ReportesDto.SalesSummaryDto;
import com.restaurante.resturante.dto.venta.ReportesDto.StockCriticalDto;
import com.restaurante.resturante.dto.venta.ReportesDto.WaiterPerformanceDto;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReporteServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReporteService reporteService;

    private Sucursal sucursal;
    private User user;
    private MedioPago medioPagoEfectivo;
    private MedioPago medioPagoTarjeta;
    private Producto producto;
    private CajaTurno cajaTurno;

    @BeforeEach
    public void setUp() {
        // 1. Crear Empresa
        Empresa empresa = Empresa.builder()
                .ruc("20123456789")
                .razonSocial("Test Empresa S.A.C.")
                .fechaAfiliacion(LocalDate.now())
                .active(true)
                .shadowBan(false)
                .build();
        entityManager.persist(empresa);

        // 2. Crear Sucursal
        sucursal = Sucursal.builder()
                .nombre("Sucursal Central")
                .empresa(empresa)
                .estado(true)
                .build();
        entityManager.persist(sucursal);

        // 3. Crear TipoDocumento
        TipoDocumento tipoDoc = TipoDocumento.builder()
                .name("DNI_TEST")
                .build();
        entityManager.persist(tipoDoc);

        // 4. Crear Role
        Role role = Role.builder()
                .name("ADMIN_TEST")
                .description("Administrador")
                .build();
        entityManager.persist(role);

        // 5. Crear User
        user = User.builder()
                .username("test_admin")
                .password("encoded_pass")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Gomez")
                .tipoDocumento(tipoDoc)
                .numeroDocumento("12345678")
                .role(role)
                .active(true)
                .build();
        entityManager.persist(user);

        // 6. Crear Medios de Pago
        medioPagoEfectivo = MedioPago.builder()
                .nombre("EFECTIVO")
                .esEfectivo(true)
                .empresa(empresa)
                .isActive(true)
                .build();
        entityManager.persist(medioPagoEfectivo);

        medioPagoTarjeta = MedioPago.builder()
                .nombre("TARJETA")
                .esEfectivo(false)
                .empresa(empresa)
                .isActive(true)
                .build();
        entityManager.persist(medioPagoTarjeta);

        // 7. CategoriaProducto
        CategoriaProducto categoria = CategoriaProducto.builder()
                .nombreCategoria("Platos Fondos")
                .sucursal(sucursal)
                .build();
        entityManager.persist(categoria);

        // 8. Crear Producto
        producto = Producto.builder()
                .nombreProducto("Ceviche Clásico")
                .precioVenta(new BigDecimal("35.00"))
                .costoCompra(new BigDecimal("15.00"))
                .categoria(categoria)
                .sucursal(sucursal)
                .unidadMedida("UNIDAD")
                .estado(true)
                .build();
        entityManager.persist(producto);

        // 9. CajaTurno
        cajaTurno = CajaTurno.builder()
                .codigoApertura("AP-001")
                .montoApertura(new BigDecimal("100.00"))
                .fechaApertura(LocalDateTime.now().minusDays(1))
                .fechaCierre(LocalDateTime.now().minusHours(2))
                .sucursal(sucursal)
                .user(user)
                .estado("CERRADA")
                .montoCierreEsperado(new BigDecimal("235.00"))
                .montoCierreReal(new BigDecimal("235.00"))
                .diferencia(BigDecimal.ZERO)
                .build();
        entityManager.persist(cajaTurno);
    }

    @Test
    public void testGetSalesSummary() {
        // GIVEN
        Pedido pedido = Pedido.builder()
                .codigoPedido("PED-001")
                .sucursal(sucursal)
                .user(user)
                .cajaTurno(cajaTurno)
                .estado("PAGADO")
                .totalFinal(new BigDecimal("70.00"))
                .fechaCreacion(LocalDateTime.now())
                .build();
        entityManager.persist(pedido);

        PedidoPago pago = PedidoPago.builder()
                .pedido(pedido)
                .monto(new BigDecimal("70.00"))
                .medioPago(medioPagoEfectivo)
                .fechaPago(LocalDateTime.now())
                .cajaTurno(cajaTurno)
                .build();
        entityManager.persist(pago);

        entityManager.flush();

        // WHEN
        SalesSummaryDto summary = reporteService.getSalesSummary(
                List.of(sucursal.getId()), 
                LocalDate.now().minusDays(1), 
                LocalDate.now().plusDays(1)
        );

        // THEN
        assertNotNull(summary);
        assertEquals(new BigDecimal("70.00"), summary.totalVentas());
        assertEquals(new BigDecimal("70.00"), summary.promedioTicket());
        assertEquals(new BigDecimal("70.00"), summary.totalEfectivo());
        assertEquals(BigDecimal.ZERO, summary.totalTarjeta());
    }

    @Test
    public void testGetMenuRanking() {
        // GIVEN
        Pedido pedido = Pedido.builder()
                .codigoPedido("PED-002")
                .sucursal(sucursal)
                .user(user)
                .cajaTurno(cajaTurno)
                .estado("PAGADO")
                .totalFinal(new BigDecimal("70.00"))
                .fechaCreacion(LocalDateTime.now())
                .build();
        entityManager.persist(pedido);

        PedidoDetalle detalle = PedidoDetalle.builder()
                .pedido(pedido)
                .producto(producto)
                .cantidad(2)
                .precioUnitario(new BigDecimal("35.00"))
                .totalLinea(new BigDecimal("70.00"))
                .build();
        entityManager.persist(detalle);

        entityManager.flush();

        // WHEN
        List<MenuRankingDto> ranking = reporteService.getMenuRanking(
                List.of(sucursal.getId()), 
                LocalDate.now().minusDays(1), 
                LocalDate.now().plusDays(1)
        );

        // THEN
        assertNotNull(ranking);
        assertTrue(ranking.size() > 0);
        assertEquals("Ceviche Clásico", ranking.get(0).nombrePlato());
        assertEquals(2L, ranking.get(0).cantidad());
        assertEquals(new BigDecimal("70.00"), ranking.get(0).ingresos());
    }

    @Test
    public void testGetCajaAudit() {
        // GIVEN
        MovimientoCaja mov = MovimientoCaja.builder()
                .tipo(TipoMovimiento.INGRESO)
                .monto(new BigDecimal("50.00"))
                .fecha(LocalDateTime.now())
                .cajaTurno(cajaTurno)
                .usuario(user)
                .esEfectivo(true)
                .build();
        entityManager.persist(mov);

        entityManager.flush();

        // WHEN
        List<CajaAuditDto> audits = reporteService.getCajaAudit(
                List.of(sucursal.getId()), 
                LocalDate.now().minusDays(2), 
                LocalDate.now().plusDays(1)
        );

        // THEN
        assertNotNull(audits);
        assertTrue(audits.size() > 0);
        CajaAuditDto dto = audits.stream()
                .filter(a -> a.cajaTurnoId().equals(cajaTurno.getId()))
                .findFirst().orElse(null);
        assertNotNull(dto);
        assertEquals(new BigDecimal("50.00"), dto.ingresosCajaChica());
    }

    @Test
    public void testGetStockCritical() {
        // GIVEN
        Inventario inventario = Inventario.builder()
                .producto(producto)
                .sucursal(sucursal)
                .stockActual(2)
                .stockMinimo(5)
                .build();
        entityManager.persist(inventario);

        entityManager.flush();

        // WHEN
        StockCriticalDto stockCritical = reporteService.getStockCritical(List.of(sucursal.getId()));

        // THEN
        assertNotNull(stockCritical);
        assertEquals(new BigDecimal("30.00"), stockCritical.valoracionTotalInventario()); // 2 * 15.00
        assertTrue(stockCritical.productosBajoStock().size() > 0);
        assertEquals("Ceviche Clásico", stockCritical.productosBajoStock().get(0).nombreProducto());
        assertEquals(new BigDecimal("2"), stockCritical.productosBajoStock().get(0).stockActual());
        assertEquals(new BigDecimal("5"), stockCritical.productosBajoStock().get(0).stockMinimo());
    }

    @Test
    public void testGetWaiterPerformance() {
        // GIVEN
        Pedido pedido = Pedido.builder()
                .codigoPedido("PED-003")
                .sucursal(sucursal)
                .user(user)
                .cajaTurno(cajaTurno)
                .estado("PAGADO")
                .totalFinal(new BigDecimal("100.00"))
                .fechaCreacion(LocalDateTime.now())
                .build();
        entityManager.persist(pedido);

        entityManager.flush();

        // WHEN
        List<WaiterPerformanceDto> performance = reporteService.getWaiterPerformance(
                List.of(sucursal.getId()), 
                LocalDate.now().minusDays(1), 
                LocalDate.now().plusDays(1)
        );

        // THEN
        assertNotNull(performance);
        assertTrue(performance.size() > 0);
        assertEquals("Juan Perez", performance.get(0).waiterNombre());
        assertEquals(1L, performance.get(0).cantidadPedidos());
        assertEquals(new BigDecimal("100.00"), performance.get(0).totalVendido());
    }
}
