package com.restaurante.resturante.dto.security;

public record LoginRequest(
    String username, 
    String password
) {}
