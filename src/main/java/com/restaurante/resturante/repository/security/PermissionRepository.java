package com.restaurante.resturante.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.security.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>{

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);
}
