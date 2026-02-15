package com.restaurante.resturante.repository.inventario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.inventario.Inventario;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProducto_IdProducto(Integer idProducto);

    @Query("SELECT DISTINCT p.proveedor FROM Inventario i JOIN i.producto p WHERE p.proveedor IS NOT NULL AND p.estado = true")
    List<Proveedor> findDistinctProveedoresConInventario();

    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto p LEFT JOIN FETCH p.proveedor prov WHERE p.proveedor.idProveedor = :idProveedor AND p.estado = true")
    List<Inventario> findByProductoProveedorIdProveedorAndProductoEstadoTrue(Integer idProveedor);
}
