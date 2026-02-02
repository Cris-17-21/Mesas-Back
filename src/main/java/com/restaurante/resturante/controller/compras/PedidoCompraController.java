package com.restaurante.resturante.controller.compras;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.compras.PedidoCompraDto;
import com.restaurante.resturante.service.compras.IPedidoCompraService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PedidoCompraController {

    private final IPedidoCompraService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoCompraDto>> getAll() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoCompraDto> getById(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PedidoCompraDto> registrarPedido(@RequestBody PedidoCompraDto dto) {
        return ResponseEntity.ok(pedidoService.registrarPedido(dto));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoCompraDto> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, estado));
    }
}
