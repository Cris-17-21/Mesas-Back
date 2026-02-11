package com.restaurante.resturante.controller.compras;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.compras.TiposPagoDto;
import com.restaurante.resturante.service.compras.ITiposPagoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tipos-pago")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TiposPagoController {

    private final ITiposPagoService tiposPagoService;

    @GetMapping
    public ResponseEntity<List<TiposPagoDto>> getAll() {
        return ResponseEntity.ok(tiposPagoService.findAll());
    }
}
