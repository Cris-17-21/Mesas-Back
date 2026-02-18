package com.restaurante.resturante.controller.inventario;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.inventario.ProductoDto;
import com.restaurante.resturante.service.inventario.IProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductoController {

    private final IProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDto>> getAll() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> getById(@PathVariable Integer id) {
        return productoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoDto> create(@RequestBody ProductoDto dto) {
        return ResponseEntity.ok(productoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDto> update(@PathVariable Integer id, @RequestBody ProductoDto dto) {
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ProductoDto>> findByEmpresaId(@PathVariable String empresaId) {
        return ResponseEntity.ok(productoService.findByEmpresaId(empresaId));
    }
}
