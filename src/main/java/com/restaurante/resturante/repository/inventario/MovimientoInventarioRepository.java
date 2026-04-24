package com.restaurante.resturante.repository.inventario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.inventario.MovimientoInventario;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    
    List<MovimientoInventario> findBySucursal_IdOrderByFechaMovimientoDesc(String sucursalId);

    List<MovimientoInventario> findByProducto_IdProductoAndSucursal_IdOrderByFechaMovimientoDesc(Integer idProducto, String sucursalId);
}
