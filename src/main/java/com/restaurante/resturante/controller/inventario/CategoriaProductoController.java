package com.restaurante.resturante.controller.inventario;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;
import com.restaurante.resturante.service.inventario.ICategoriaProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaProductoController {

    private final ICategoriaProductoService categoriaService;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<CategoriaProductoDto>> findByEmpresaId(@PathVariable String empresaId) {
        return ResponseEntity.ok(categoriaService.findByEmpresaId(empresaId));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaProductoDto>> findAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaProductoDto> getById(@PathVariable Integer id) {
        CategoriaProductoDto dto = categoriaService.findById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<CategoriaProductoDto> create(@RequestBody CategoriaProductoDto dto) {
        return ResponseEntity.ok(categoriaService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaProductoDto> update(@PathVariable Integer id,
            @RequestBody CategoriaProductoDto dto) {
        CategoriaProductoDto updated = categoriaService.update(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
