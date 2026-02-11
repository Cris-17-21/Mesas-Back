package com.restaurante.resturante.repository.venta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.ventas.CajaTurno;

public interface CajaTurnoRepository extends JpaRepository<CajaTurno, String>{

    // Buscar el turno activo de un usuario en una sucursal
    Optional<CajaTurno> findByUserIdAndSucursalIdAndEstado(String usuarioId, String sucursalId, String estado);

    // Verificar si existe turno abierto
    boolean existsByUserIdAndSucursalIdAndEstado(String usuarioId, String sucursalId, String estado);
}
