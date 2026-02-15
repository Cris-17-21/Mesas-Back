package com.restaurante.resturante.repository.venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.ventas.CajaTurno;

public interface CajaTurnoRepository extends JpaRepository<CajaTurno, String> {

    // 1. VALIDACIÓN VITAL: ¿Hay una caja abierta en esta sucursal?
    // Busca un turno que pertenezca a la sucursal y NO tenga fecha de cierre.
    Optional<CajaTurno> findBySucursalIdAndFechaCierreIsNull(String sucursalId);

    // Búsqueda específica por usuario y estado (para validar si YA tiene una
    // abierta)
    Optional<CajaTurno> findByUserIdAndSucursalIdAndEstado(String userId, String sucursalId, String estado);

    // Versión booleana (más rápida para validaciones antes de crear)
    boolean existsBySucursalIdAndFechaCierreIsNull(String sucursalId);

    // Validación por estado explícito
    boolean existsBySucursalIdAndEstado(String sucursalId, String estado);

    // 2. HISTORIAL: Buscar turnos por rango de fechas (Reporte de cierres)
    List<CajaTurno> findBySucursalIdAndFechaAperturaBetween(String sucursalId, LocalDateTime inicio, LocalDateTime fin);

    // 3. SEGURIDAD: Buscar turnos de un usuario específico (Mis turnos)
    List<CajaTurno> findByUserId(String userId);

    // 4. ESTADÍSTICA: Buscar turnos con diferencias (donde faltó o sobró dinero)
    // Útil para auditar cajeros sospechosos
    @Query("SELECT c FROM CajaTurno c WHERE c.sucursal.id = :sucursalId AND c.diferencia <> 0")
    List<CajaTurno> findTurnosConDiferencia(String sucursalId);
}
