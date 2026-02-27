package com.restaurante.resturante.repository.maestro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurante.resturante.domain.maestros.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, String> {

    // Listar todas las empresas activas
    List<Sucursal> findAllByEstadoTrue();

    List<Sucursal> findByEmpresaIdAndEstadoTrue(String empresaId);

    @Query("SELECT s FROM Sucursal s JOIN s.usersAccess ua JOIN ua.user u WHERE u.username = :username")
    List<Sucursal> findByUsuariosUsername(@Param("username") String username);
}
