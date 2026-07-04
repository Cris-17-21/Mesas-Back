package com.restaurante.resturante.service.venta.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.resturante.domain.inventario.Producto;
import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.ventas.FacturacionComprobante;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionApiComprobanteService;
import com.restaurante.resturante.service.maestros.jpa.ClienteService;

@ExtendWith(MockitoExtension.class)
public class FacturacionComprobanteServiceTest {

        @Mock
        private FacturacionComprobanteRepository facturacionRepository;

        @Mock
        private PedidoRepository pedidoRepository;

        @Mock
        private FacturacionApiComprobanteService comprobanteApiService;

        @Mock
        private ClienteService clienteService;

        @Mock
        private com.restaurante.resturante.repository.venta.FacturacionSerieRepository serieLocalRepository;

        @Mock
        private com.restaurante.resturante.service.maestros.IMesaService mesaService;

        @InjectMocks
        private FacturacionComprobanteService facturacionService;

        @Test
        public void testEmitirComprobanteExitoso() {
                // Mock data: tipo "01" = Factura, requiere cliente con RUC de 11 dígitos
                String pedidoId = "ped-123";
                FacturaRequestDto requestDto = new FacturaRequestDto(pedidoId, "01", null, null, null, null, false);

                Empresa empresa = Empresa.builder()
                                .ruc("20123456789")
                                .razonSocial("Test Empresa")
                                .direccionFiscal("Av. Lima 123")
                                .build();
                Sucursal sucursal = Sucursal.builder()
                                .id("suc-001")
                                .empresa(empresa)
                                .nombre("Sede Central")
                                .build();

                // Cliente con RUC válido de 11 dígitos (requerido para Factura)
                TipoDocumento tipoDoc = TipoDocumento.builder().name("RUC").build();
                Cliente cliente = Cliente.builder()
                                .tipoDocumento(tipoDoc)
                                .numeroDocumento("20987654321")
                                .nombreRazonSocial("Empresa Compradora SAC")
                                .build();

                Producto producto = Producto.builder()
                                .idProducto(1)
                                .nombreProducto("Lomo Saltado")
                                .precioVenta(new BigDecimal("35.00"))
                                .build();

                PedidoDetalle detalle = PedidoDetalle.builder()
                                .producto(producto)
                                .cantidad(1)
                                .precioUnitario(new BigDecimal("35.00"))
                                .totalLinea(new BigDecimal("35.00"))
                                .build();

                Pedido pedido = Pedido.builder()
                                .id(pedidoId)
                                .sucursal(sucursal)
                                .cliente(cliente)
                                .totalFinal(new BigDecimal("35.00"))
                                .pedidoDetalles(new ArrayList<>())
                                .build();
                pedido.getPedidoDetalles().add(detalle);

                // Mock de apiResponse para el servicio inmediato
                com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionResponse apiResponse = new com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionResponse(
                                "api-id",
                                "01",
                                "F001",
                                1,
                                "F001-00000001",
                                java.time.LocalDateTime.now().toString(),
                                "PEN",
                                new BigDecimal("29.66"),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                new BigDecimal("5.34"),
                                new BigDecimal("35.00"),
                                "ACEPTADO",
                                null,
                                null,
                                null,
                                "hash123",
                                null
                );
                when(comprobanteApiService.emitir(any(Pedido.class), anyString(), any(), any(), any())).thenReturn(apiResponse);

                // Mocks de repositorio
                when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
                when(facturacionRepository.save(any(FacturacionComprobante.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Execution
                FacturacionComprobanteDto result = facturacionService.emitirComprobante(requestDto);

                // Verification
                assertNotNull(result);
                assertEquals("20123456789", result.rucEmisor());
        }

        @Test
        public void testEmitirComprobanteFallbackLocal() {
                String pedidoId = "ped-123";
                FacturaRequestDto requestDto = new FacturaRequestDto(pedidoId, "01", null, null, null, null, false);

                Empresa empresa = Empresa.builder()
                                .ruc("20123456789")
                                .razonSocial("Test Empresa")
                                .direccionFiscal("Av. Lima 123")
                                .build();
                Sucursal sucursal = Sucursal.builder()
                                .id("suc-001")
                                .empresa(empresa)
                                .nombre("Sede Central")
                                .build();

                TipoDocumento tipoDoc = TipoDocumento.builder().name("RUC").build();
                Cliente cliente = Cliente.builder()
                                .tipoDocumento(tipoDoc)
                                .numeroDocumento("20987654321")
                                .nombreRazonSocial("Empresa Compradora SAC")
                                .build();

                Producto producto = Producto.builder()
                                .idProducto(1)
                                .nombreProducto("Lomo Saltado")
                                .precioVenta(new BigDecimal("35.00"))
                                .build();

                PedidoDetalle detalle = PedidoDetalle.builder()
                                .producto(producto)
                                .cantidad(1)
                                .precioUnitario(new BigDecimal("35.00"))
                                .totalLinea(new BigDecimal("35.00"))
                                .build();

                Pedido pedido = Pedido.builder()
                                .id(pedidoId)
                                .sucursal(sucursal)
                                .cliente(cliente)
                                .totalFinal(new BigDecimal("35.00"))
                                .pedidoDetalles(new ArrayList<>())
                                .build();
                pedido.getPedidoDetalles().add(detalle);

                com.restaurante.resturante.domain.ventas.FacturacionSerie serieLocal = com.restaurante.resturante.domain.ventas.FacturacionSerie.builder()
                                .tipoComprobante("01")
                                .serie("F001")
                                .proximoCorrelativo(5)
                                .activo(true)
                                .sucursal(sucursal)
                                .empresa(empresa)
                                .build();

                // Mock API to throw an exception
                when(comprobanteApiService.emitir(any(Pedido.class), anyString(), any(), any(), any()))
                                .thenThrow(new RuntimeException("Connection Timeout"));

                // Mock Local Series and Max Correlativo
                when(serieLocalRepository.findBySucursalIdAndTipoComprobanteAndActivoTrue("suc-001", "01"))
                                .thenReturn(Optional.of(serieLocal));
                when(facturacionRepository.obtenerMaxCorrelativo("suc-001", "01", "F001"))
                                .thenReturn(3);

                // Mock Repositories
                when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
                when(facturacionRepository.save(any(FacturacionComprobante.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Execution
                FacturacionComprobanteDto result = facturacionService.emitirComprobante(requestDto);

                // Verification
                assertNotNull(result);
                assertEquals("20123456789", result.rucEmisor());
                assertEquals("PENDIENTE_ENVIO", result.estadoSunat());
                assertEquals("F001", result.serie());
                assertEquals("00000005", result.correlativo()); // max(3+1, 5) = 5
                assertEquals(6, serieLocal.getProximoCorrelativo()); // incremented to 6
        }
}
