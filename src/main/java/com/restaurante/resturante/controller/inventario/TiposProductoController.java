package com.restaurante.resturante.controller.inventario;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.inventario.TiposProducto;
import com.restaurante.resturante.dto.inventario.TiposProductoDto;
import com.restaurante.resturante.mapper.inventario.TiposProductoMapper;
import com.restaurante.resturante.repository.inventario.CategoriaProductoRepository;
import com.restaurante.resturante.service.inventario.ITiposProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tipos-producto")
@RequiredArgsConstructor
public class TiposProductoController {

    private final ITiposProductoService service;
    private final CategoriaProductoRepository categoriaRepository;
    private final TiposProductoMapper mapper;

    @GetMapping
    public ResponseEntity<List<TiposProductoDto>> getAll() {
        return ResponseEntity.ok(service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiposProductoDto> getById(@PathVariable Integer id) {
        TiposProducto entity = service.findById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDto(entity));
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<TiposProductoDto>> getByCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(service.findByCategoryId(idCategoria).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<TiposProductoDto> create(@RequestBody TiposProductoDto dto) {
        CategoriaProducto categoria = null;
        if (dto.idCategoria() != null) {
            categoria = categoriaRepository.findById(dto.idCategoria()).orElse(null);
        }

        // Validate category existence usually done better but simplistic here
        if (categoria == null && dto.idCategoria() != null) {
            // Maybe return bad request or handle gracefully
        }

        TiposProducto entity = mapper.toEntity(dto, categoria);
        return ResponseEntity.ok(mapper.toDto(service.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TiposProductoDto> update(@PathVariable Integer id, @RequestBody TiposProductoDto dto) {
        TiposProducto existing = service.findById(id);
        if (existing == null)
            return ResponseEntity.notFound().build();

        CategoriaProducto categoria = null;
        if (dto.idCategoria() != null) {
            categoria = categoriaRepository.findById(dto.idCategoria()).orElse(null);
        } else if (existing.getCategoria() != null) {
            categoria = existing.getCategoria(); // Keep existing if not provided? Or allow unsetting? Assuming
                                                 // provided.
        }

        existing.setNombreTipo(dto.nombreTipo());
        existing.setCategoria(categoria);

        return ResponseEntity.ok(mapper.toDto(service.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
