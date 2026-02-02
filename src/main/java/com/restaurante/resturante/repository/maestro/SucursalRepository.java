package com.restaurante.resturante.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, String>{
    List<Sucursal> findByEmpresaId(String empresaId);
}
