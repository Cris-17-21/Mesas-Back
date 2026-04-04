package com.restaurante.resturante.repository.inventario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.inventario.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByEstadoTrue();

    List<Producto> findBySucursal_IdAndEstadoTrue(String sucursalId);

    List<Producto> findBySucursal_IdAndEstadoTrueAndEsPlatoTrue(String sucursalId);

    List<Producto> findByEstadoTrueAndEsPlatoTrue();

    // Legacy method
    List<Producto> findBySucursal_Empresa_IdAndEstadoTrue(String empresaId);
}
