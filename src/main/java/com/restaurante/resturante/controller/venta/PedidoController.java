package com.restaurante.resturante.controller.venta;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.UnionMesaRequest;
import com.restaurante.resturante.dto.venta.PedidoDetalleRequestDto;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResumenDto;
import com.restaurante.resturante.service.venta.IPedidoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final IPedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDto> crearPedido(@RequestBody PedidoRequestDto dto) {
        // PERMISO: REGISTRAR_PEDIDO
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(dto));
    }

    @GetMapping("/activos/{sucursalId}")
    public ResponseEntity<List<PedidoResumenDto>> listarActivos(@PathVariable String sucursalId) {
        // PERMISO: VER_PEDIDOS_ACTIVOS
        return ResponseEntity.ok(pedidoService.listarPedidosActivos(sucursalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDto> verDetalle(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    // Para agregar más platos a una mesa (comanda adicional)
    @PatchMapping("/{id}/detalles")
    public ResponseEntity<PedidoResponseDto> agregarPlatos(
            @PathVariable String id, 
            @RequestBody List<PedidoDetalleRequestDto> nuevosDetalles) {
        // PERMISO: ACTUALIZAR_COMANDA
        return ResponseEntity.ok(pedidoService.actualizarDetalles(id, nuevosDetalles));
    }

    @PostMapping("/unir-mesas")
    public ResponseEntity<Void> unirMesas(@RequestBody UnionMesaRequest dto) {
        // PERMISO: UNIR_MESAS
        pedidoService.unirMesas(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<Void> registrarPago(
            @PathVariable String id, 
            @RequestParam String metodoPago) {
        // PERMISO: REGISTRAR_PAGO
        pedidoService.registrarPago(id, metodoPago);
        return ResponseEntity.noContent().build();
    }
}
