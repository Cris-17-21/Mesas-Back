package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurante.resturante.domain.maestros.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, String> {

    // Listar todas las empresas activas ordenadas por fecha de creación
    List<Sucursal> findAllByEstadoTrueOrderByCreatedDateAsc();

    List<Sucursal> findByEmpresaIdAndEstadoTrueOrderByCreatedDateAsc(String empresaId);

    List<Sucursal> findByEmpresaId(String empresaId);

    Optional<Sucursal> findByNombreIgnoreCaseAndEmpresaId(String nombre, String empresaId);

    Optional<Sucursal> findByNombreIgnoreCaseAndEmpresaIdAndEstadoTrue(String nombre, String empresaId);

    @Query("SELECT s FROM Sucursal s JOIN s.usersAccess ua JOIN ua.user u WHERE u.username = :username")
    List<Sucursal> findByUsuariosUsername(@Param("username") String username);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.sucursal.id = :sucursalId")
    long countProductosBySucursalId(@Param("sucursalId") String sucursalId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.sucursal.id = :sucursalId")
    long countPedidosBySucursalId(@Param("sucursalId") String sucursalId);

    @Query("SELECT COUNT(pc) FROM PedidoCompra pc WHERE pc.sucursal.id = :sucursalId")
    long countPedidosCompraBySucursalId(@Param("sucursalId") String sucursalId);

    @Query("SELECT COUNT(fc) FROM FacturacionComprobante fc WHERE fc.sucursal.id = :sucursalId")
    long countComprobantesBySucursalId(@Param("sucursalId") String sucursalId);

    @Query("SELECT COUNT(ct) FROM CajaTurno ct WHERE ct.sucursal.id = :sucursalId")
    long countCajasBySucursalId(@Param("sucursalId") String sucursalId);

    @Query("SELECT COUNT(p) FROM Piso p WHERE p.sucursal.id = :sucursalId")
    long countPisosBySucursalId(@Param("sucursalId") String sucursalId);
}
