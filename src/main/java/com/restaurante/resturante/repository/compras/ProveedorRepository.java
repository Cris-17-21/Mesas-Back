package com.restaurante.resturante.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.Proveedor;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    List<Proveedor> findByEmpresa_Id(String empresaId);
}
