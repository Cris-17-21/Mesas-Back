package com.restaurante.resturante.controller.venta;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.service.venta.jpa.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDto> crear(@Valid @RequestBody PedidoRequestDto request) {
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDto>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDto> actualizarEstado(
            @PathVariable String id, 
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }
}
