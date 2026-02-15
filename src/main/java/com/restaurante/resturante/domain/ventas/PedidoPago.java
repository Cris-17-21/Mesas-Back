package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.maestros.MedioPago;

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
@Table(name = "pedido_pagos")
public class PedidoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "referencia_pago")
    private String referenciaPago; // Nro Operación, 4 últimos dígitos tarjeta, etc.

    // RELACIONES

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER) // Eager porque casi siempre necesitamos ver el nombre del medio de pago
    @JoinColumn(name = "medio_pago_id", nullable = false)
    private MedioPago medioPago;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_turno_id", nullable = false) 
    private CajaTurno cajaTurno; // VITAL: Saber a qué turno pertenece este dinero específico
}
