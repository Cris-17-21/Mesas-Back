package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.ClienteDto;
import com.restaurante.resturante.dto.maestro.CreateClienteDto;
import com.restaurante.resturante.service.maestros.IClienteService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final IClienteService clienteService;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ClienteDto>> listarPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(clienteService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/documento/{num}")
    public ResponseEntity<ClienteDto> buscar(@PathVariable String num) {
        return ResponseEntity.ok(clienteService.buscarPorDocumento(num));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteDto> crear(@RequestBody CreateClienteDto dto) {
        return new ResponseEntity<>(clienteService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<ClienteDto> actualizar(@PathVariable String id, @RequestBody CreateClienteDto dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
