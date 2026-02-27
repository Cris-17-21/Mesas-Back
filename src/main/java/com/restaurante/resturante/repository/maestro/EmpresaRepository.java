package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    // Lista de empresas activas
    List<Empresa> findAllByActiveTrue();

    // Este es el que te faltaba para el Service
    boolean existsByRuc(String ruc);

    // También es útil tener este por si necesitas buscar por RUC
    Optional<Empresa> findByRuc(String ruc);
}
