package com.restaurante.resturante.repository.inventario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.inventario.CategoriaProducto;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Integer> {
    List<CategoriaProducto> findBySucursal_Id(String sucursalId);
    
    // Legacy method
    List<CategoriaProducto> findBySucursal_Empresa_Id(String empresaId);
}
