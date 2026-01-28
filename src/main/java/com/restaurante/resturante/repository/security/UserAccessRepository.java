package com.restaurante.resturante.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.security.UserAccess;

public interface UserAccessRepository extends JpaRepository<UserAccess, String>{

    List<UserAccess> findByUserIdAndActiveTrue(String userId);

    Optional<UserAccess> findByUserIdAndSucursalId(String userId, String sucursalId);

}
