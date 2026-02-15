package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.restaurante.resturante.domain.security.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo; // INGRESO, EGRESO

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(columnDefinition = "TEXT")
    private String descripcion; // Ej: "Compra de hielo", "Pago proveedor"

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    // RELACIONES

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_turno_id", nullable = false)
    @ToString.Exclude
    private CajaTurno cajaTurno; // Afecta al turno actual

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario; // Quién hizo el movimiento
}

