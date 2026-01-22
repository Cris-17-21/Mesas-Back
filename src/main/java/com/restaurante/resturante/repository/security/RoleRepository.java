package com.restaurante.resturante.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.security.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>{

    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
}
