package com.restaurante.resturante.controller.auxiliar;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.auxiliar.EmpresaDto;
import com.restaurante.resturante.dto.auxiliar.PersonaDto;
import com.restaurante.resturante.service.auxiliar.jpa.ConsultaSunatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaSunatService consultaSunatService;

    @GetMapping("/dni/{dni}")
    public ResponseEntity<PersonaDto> consultarDni(@PathVariable String dni) {
        return ResponseEntity.ok(consultaSunatService.consultarDni(dni));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<EmpresaDto> consultarRuc(@PathVariable String ruc) {
        return ResponseEntity.ok(consultaSunatService.consultarRuc(ruc));
    }
}
