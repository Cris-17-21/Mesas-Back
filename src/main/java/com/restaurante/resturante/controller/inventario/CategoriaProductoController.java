package com.restaurante.resturante.controller.inventario;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
