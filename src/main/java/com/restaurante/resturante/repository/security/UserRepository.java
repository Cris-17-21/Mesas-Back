package com.restaurante.resturante.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.security.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username); // Encontrar usuario por el username

    Boolean existsByUsername(String username); // Si existe el usuario por el username

    List<User> findAllByActiveTrue();

    Optional<User> findByNumeroDocumento(String numeroDocumento);

    Boolean existsByNumeroDocumentoAndActiveTrue(String numeroDocumento);

    /**
     * Busca un usuario por su username y carga de forma anticipada (EAGER)
     * todas las relaciones necesarias para el endpoint /me, evitando problemas de
     * LazyInitialization.
     * 
     * @param username El username a buscar.
     * @return Un Optional con el usuario y todos sus detalles cargados.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role r " +
            "LEFT JOIN FETCH r.permissions p " +
            "LEFT JOIN FETCH p.module " +
            "LEFT JOIN FETCH u.tipoDocumento " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithDetails(@Param("username") String username);
}
