package com.restaurante.resturante.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, String>{

    // Buscar mesas por área/piso (ej: Segundo Piso)
    List<Mesa> findByPisoId(String pisoId);

    // Buscar mesas por estado (ej: Ver todas las Libres)
    List<Mesa> findByEstadoAndActiveTrue(String estado);
}
