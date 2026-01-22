package com.restaurante.resturante.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.security.RefreshToken;
import com.restaurante.resturante.domain.security.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>{

    Optional<RefreshToken> findByToken (String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(User user);
}
