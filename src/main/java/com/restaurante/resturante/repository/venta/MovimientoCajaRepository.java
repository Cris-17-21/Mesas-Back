package com.restaurante.resturante.repository.venta;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurante.resturante.domain.ventas.MovimientoCaja;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, String> {

    // 1. LISTADO: Ver todos los movimientos (gastos/ingresos) de un turno
    // específico
    // Nota: Usamos String para el ID del turno porque es un UUID
    List<MovimientoCaja> findByCajaTurnoId(String cajaTurnoId);

    // 1.1 ORDENADO POR FECHA
    List<MovimientoCaja> findByCajaTurnoIdOrderByFechaDesc(String cajaTurnoId);

    // 2. CÁLCULO PARA ARQUEO: Sumar total de Ingresos o Egresos de un turno
    // El COALESCE es vital: si no hay movimientos, devuelve 0 en vez de null (evita
    // NullPointerException)
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.cajaTurno.id = :cajaTurnoId AND m.tipo = :tipo")
    BigDecimal sumarPorTipoYTurno(String cajaTurnoId, TipoMovimiento tipo);

    // 3. SEGURIDAD: Buscar movimientos hechos por un usuario específico (Auditoría)
    // Asumiendo que guardas el usuario que hizo el movimiento
    List<MovimientoCaja> findByUsuarioId(String usuarioId);
}
