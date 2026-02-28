package com.restaurante.resturante.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;

public interface UserAccessRepository extends JpaRepository<UserAccess, String> {

    List<UserAccess> findByUserIdAndSucursalId(String userId, String sucursalId);

    List<UserAccess> findByUserIdAndEmpresaId(String userId, String empresaId);

    List<UserAccess> findByUserIdAndActiveTrue(String userId);

    List<UserAccess> findByEmpresaId(String empresaId);

    List<UserAccess> findBySucursalId(String sucursalId);

    Optional<UserAccess> findByUserId(String userId);

    // 1. Buscamos Usuarios filtrando por la Empresa en la tabla de accesos
    @Query("SELECT DISTINCT a.user FROM UserAccess a WHERE a.empresa.id = :empresaId AND a.active = true")
    List<User> findActiveUsersByEmpresaId(@Param("empresaId") String empresaId);

    // 2. Buscamos Usuarios filtrando por Empresa y Sucursal en la tabla de accesos
    @Query("SELECT DISTINCT a.user FROM UserAccess a WHERE a.empresa.id = :empresaId AND a.sucursal.id = :sucursalId")
    List<User> findUsersByEmpresaIdAndSucursalId(@Param("empresaId") String empresaId,
            @Param("sucursalId") String sucursalId);

    Optional<UserAccess> findByUserUsernameAndSucursalId(String username, String sucursalId);

    // Para buscar cualquier acceso del usuario y obtener su empresa
    List<UserAccess> findByUserUsername(String username);
}
