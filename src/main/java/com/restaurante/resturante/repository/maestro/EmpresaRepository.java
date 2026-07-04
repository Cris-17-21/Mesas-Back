package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.dashboard.EmpresaUserStatsDto;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    // Lista de empresas activas
    List<Empresa> findAllByActiveTrue();

    // Este es el que te faltaba para el Service
    boolean existsByRuc(String ruc);

    // También es útil tener este por si necesitas buscar por RUC
    Optional<Empresa> findByRuc(String ruc);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.empresa.id = :empresaId")
    long countClientesByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(p) FROM Proveedor p WHERE p.empresa.id = :empresaId")
    long countProveedoresByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.sucursal.empresa.id = :empresaId")
    long countProductosByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.sucursal.empresa.id = :empresaId")
    long countPedidosByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(pc) FROM PedidoCompra pc WHERE pc.sucursal.empresa.id = :empresaId")
    long countPedidosCompraByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(fc) FROM FacturacionComprobante fc WHERE fc.empresa.id = :empresaId")
    long countComprobantesByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT COUNT(ct) FROM CajaTurno ct WHERE ct.sucursal.empresa.id = :empresaId")
    long countCajasByEmpresaId(@Param("empresaId") String empresaId);

    @Query("SELECT new com.restaurante.resturante.dto.dashboard.EmpresaUserStatsDto(" +
           "e.id, e.ruc, e.razonSocial, e.email, e.telefono, " +
           "(SELECT COUNT(DISTINCT ua.user.id) FROM UserAccess ua WHERE ua.empresa.id = e.id), " +
           "(SELECT COUNT(s) FROM Sucursal s WHERE s.empresa.id = e.id AND s.estado = true)) " +
           "FROM Empresa e ORDER BY e.razonSocial ASC")
    List<EmpresaUserStatsDto> getEmpresasUserStats();
}
