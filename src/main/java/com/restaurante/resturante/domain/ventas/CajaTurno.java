package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "caja_turnos")
public class CajaTurno extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "monto_apertura", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre_esperado", precision = 12, scale = 2)
    private BigDecimal montoCierreEsperado; // Lo que el sistema dice que hay

    @Column(name = "monto_cierre_real", precision = 12, scale = 2)
    private BigDecimal montoCierreReal; // Lo que el cajero cuenta

    @Column(name = "diferencia", precision = 12, scale = 2)
    private BigDecimal diferencia; // Real - Esperado

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre", nullable = true)
    private LocalDateTime fechaCierre;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones; // Por si hay sobrante o faltante

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private String estado = "ABIERTA"; 

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // ---- RELACIONES ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private User user;
}
