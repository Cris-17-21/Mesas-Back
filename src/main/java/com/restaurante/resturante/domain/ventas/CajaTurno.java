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

    @Column(name = "monto_apertura", nullable = false)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre", nullable = true)
    private BigDecimal montoCierre;

    @Column(name = "diferencia", nullable = true)
    private BigDecimal diferencia;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre", nullable = true)
    private LocalDateTime fechaCierre;

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
