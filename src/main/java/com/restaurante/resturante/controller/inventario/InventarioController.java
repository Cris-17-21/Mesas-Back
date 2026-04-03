package com.restaurante.resturante.controller.inventario;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.dto.inventario.InventarioDto;
import com.restaurante.resturante.service.inventario.IInventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final IInventarioService inventarioService;

    @GetMapping("/productos/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('READ_COMPRA', 'VER_INVENTARIO')")
    public ResponseEntity<List<InventarioDto>> listarProductosInventario(@PathVariable String sucursalId) {
        try {
            return ResponseEntity.ok(inventarioService.listarProductosInventario(sucursalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/proveedores/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('READ_COMPRA', 'VER_INVENTARIO')")
    public ResponseEntity<List<Proveedor>> listarProveedoresConInventario(@PathVariable String sucursalId) {
        try {
            return ResponseEntity.ok(inventarioService.listarProveedoresConInventario(sucursalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/productos/proveedor/{idProveedor}/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('READ_COMPRA', 'VER_INVENTARIO')")
    public ResponseEntity<List<InventarioDto>> listarProductosPorProveedor(@PathVariable Integer idProveedor, @PathVariable String sucursalId) {
        try {
            return ResponseEntity.ok(inventarioService.listarProductosPorProveedor(idProveedor, sucursalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
