package com.restaurante.resturante.controller.maestros;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.maestro.CreateMedioPagoDto;
import com.restaurante.resturante.dto.maestro.MedioPagoDto;
import com.restaurante.resturante.service.maestros.jpa.MedioPagoService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/medios-pago")
@RequiredArgsConstructor
public class MedioPagoController {

    private final MedioPagoService service;

    /**
     * 1. LISTAR TODOS
     * GET /api/medios-pago?empresaId=1
     */
    @GetMapping
    public ResponseEntity<List<MedioPagoDto>> listar(@RequestParam String empresaId) {
        return ResponseEntity.ok(service.listar(empresaId));
    }

    /**
     * 2. OBTENER UNO POR ID
     * GET /api/medios-pago/{id}?empresaId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedioPagoDto> obtenerPorId(@PathVariable String id, 
                                                     @RequestParam String empresaId) {
        return ResponseEntity.ok(service.obtenerPorId(id, empresaId));
    }

    /**
     * 3. CREAR NUEVO
     * POST /api/medios-pago
     * Body: { "nombre": "Yape", "empresaId": 1, ... }
     */
    @PostMapping
    public ResponseEntity<MedioPagoDto> crear(@RequestBody @Valid CreateMedioPagoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    /**
     * 4. ACTUALIZAR EXISTENTE
     * PUT /api/medios-pago/{id}
     * Body: { "nombre": "Yape Nuevo", "empresaId": 1, ... }
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedioPagoDto> actualizar(@PathVariable String id, 
                                                   @RequestBody @Valid CreateMedioPagoDto dto) {
        // Nota: El servicio validará que el ID coincida con la empresa del DTO
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    /**
     * 5. ELIMINAR (Soft Delete)
     * DELETE /api/medios-pago/{id}?empresaId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id, 
                                         @RequestParam String empresaId) {
        service.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
