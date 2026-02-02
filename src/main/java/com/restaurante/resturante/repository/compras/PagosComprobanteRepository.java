package com.restaurante.resturante.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.PagosComprobante;

@Repository
public interface PagosComprobanteRepository extends JpaRepository<PagosComprobante, Long> {
}
