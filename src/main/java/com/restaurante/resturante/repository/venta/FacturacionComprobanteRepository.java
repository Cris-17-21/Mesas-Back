package com.restaurante.resturante.repository.venta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.ventas.FacturacionComprobante;

@Repository
public interface FacturacionComprobanteRepository extends JpaRepository<FacturacionComprobante, String> {
    Optional<FacturacionComprobante> findByPedidoId(String pedidoId);

    Optional<FacturacionComprobante> findBySerieAndCorrelativo(String serie, String correlativo);

    java.util.List<FacturacionComprobante> findBySucursalIdOrderByFechaEmisionDesc(String sucursalId);

    java.util.List<FacturacionComprobante> findByEstadoSunat(String estadoSunat);

    @Query("SELECT c FROM FacturacionComprobante c WHERE c.sucursal.id = :sucursalId " +
           "AND (:tipo IS NULL OR c.tipoComprobante = :tipo) " +
           "AND (:serie IS NULL OR c.serie = :serie) " +
           "AND (:correlativo IS NULL OR c.correlativo = :correlativo) " +
           "AND (:inicio IS NULL OR c.fechaEmision >= :inicio) " +
           "AND (:fin IS NULL OR c.fechaEmision <= :fin) " +
           "ORDER BY c.fechaEmision DESC")
    java.util.List<FacturacionComprobante> buscarComprobantes(
            String sucursalId, String tipo, String serie, String correlativo,
            java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    @Query("SELECT MAX(CAST(c.correlativo AS int)) FROM FacturacionComprobante c WHERE c.sucursal.id = :sucursalId AND c.tipoComprobante = :tipo AND c.serie = :serie")
    Integer obtenerMaxCorrelativo(String sucursalId, String tipo, String serie);
}
