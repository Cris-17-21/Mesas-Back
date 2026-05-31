package com.restaurante.resturante.repository.venta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.ventas.FacturacionSerie;

@Repository
public interface FacturacionSerieRepository extends JpaRepository<FacturacionSerie, String> {

    List<FacturacionSerie> findBySucursalIdAndActivoTrue(String sucursalId);

    List<FacturacionSerie> findByEmpresaIdAndActivoTrue(String empresaId);

    Optional<FacturacionSerie> findBySucursalIdAndTipoComprobanteAndSerie(
            String sucursalId, String tipoComprobante, String serie);

    Optional<FacturacionSerie> findBySucursalIdAndTipoComprobanteAndActivoTrue(
            String sucursalId, String tipoComprobante);

    boolean existsByEmpresaIdAndTipoComprobanteAndSerieAndSucursalIdNot(
            String empresaId, String tipoComprobante, String serie, String sucursalId);
}
