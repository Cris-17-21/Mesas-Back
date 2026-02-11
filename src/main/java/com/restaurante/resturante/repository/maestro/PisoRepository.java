package com.restaurante.resturante.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.Piso;

public interface PisoRepository extends JpaRepository<Piso, String>{

    List<Piso> findBySucursalIdAndActiveTrue(String sucursalId);
}
