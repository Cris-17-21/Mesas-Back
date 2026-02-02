package com.restaurante.resturante.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.TiposPago;

@Repository
public interface TiposPagoRepository extends JpaRepository<TiposPago, Integer> {
}
