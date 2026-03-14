package com.restaurante.resturante.service.venta.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import com.restaurante.resturante.dto.venta.apisperu.ApisPeruDocumentResponse;
import com.restaurante.resturante.dto.venta.apisperu.ApisPeruInvoiceRequest;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository;
import com.restaurante.resturante.repository.venta.PedidoRepository;

import reactor.core.publisher.Mono;

public class FacturacionComprobanteServiceTest {

    @Mock
    private FacturacionComprobanteRepository facturacionRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ApisPeruService apisPeruService;

    @InjectMocks
    private FacturacionComprobanteService facturacionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEmitirComprobanteExitoso() {
        // Mock data
        String pedidoId = "ped-123";
        FacturaRequestDto requestDto = new FacturaRequestDto(pedidoId, "01", null, null, null);

        Empresa empresa = Empresa.builder().ruc("20123456789").razonSocial("Test Empresa").direccionFiscal("Av. Lima")
                .build();
        Sucursal sucursal = Sucursal.builder().empresa(empresa).nombre("Sede Central").build();
        TipoDocumento tipoDoc = TipoDocumento.builder().name("DNI").build();
        Cliente cliente = Cliente.builder().tipoDocumento(tipoDoc).numeroDocumento("12345678")
                .nombreRazonSocial("Juan Perez").build();

        Producto producto = Producto.builder().idProducto(1).nombreProducto("Lomo Saltado")
                .precioVenta(new BigDecimal("35.00")).build();

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

        ApisPeruDocumentResponse apiResponse = new ApisPeruDocumentResponse();
        apiResponse.setSuccess(true);
        apiResponse.setXml("https://apisperu.com/xml/test.xml");
        apiResponse.setPdf("https://apisperu.com/pdf/test.pdf");
        apiResponse.setHash("abc123hash");

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(apisPeruService.enviarFactura(any(ApisPeruInvoiceRequest.class))).thenReturn(Mono.just(apiResponse));
        when(facturacionRepository.save(any(FacturacionComprobante.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Execution
        FacturacionComprobanteDto result = facturacionService.emitirComprobante(requestDto);

        // Verification
        assertNotNull(result);
        assertEquals("https://apisperu.com/xml/test.xml", result.archivoXml());
        assertEquals("https://apisperu.com/pdf/test.pdf", result.archivoPdf());
        assertEquals("20123456789", result.rucEmisor());
    }
}
