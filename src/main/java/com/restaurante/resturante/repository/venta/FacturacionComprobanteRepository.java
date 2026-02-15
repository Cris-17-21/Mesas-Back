package com.restaurante.resturante.repository.venta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.ventas.FacturacionComprobante;

@Repository
public interface FacturacionComprobanteRepository extends JpaRepository<FacturacionComprobante, String> {
    Optional<FacturacionComprobante> findByPedidoId(String pedidoId);

    Optional<FacturacionComprobante> findBySerieAndCorrelativo(String serie, String correlativo);
}
