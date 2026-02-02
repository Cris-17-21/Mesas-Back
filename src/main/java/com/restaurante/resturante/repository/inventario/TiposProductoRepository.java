package com.restaurante.resturante.repository.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.inventario.TiposProducto;

@Repository
public interface TiposProductoRepository extends JpaRepository<TiposProducto, Integer> {
}
