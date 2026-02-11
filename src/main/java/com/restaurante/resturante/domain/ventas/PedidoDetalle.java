package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.inventario.Producto;

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
@Table(name = "pedido_detalles")
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_pagada")
    @Builder.Default
    private Integer cantidadPagada = 0; 

    @Column(name = "estado_pago")
    @Builder.Default
    private String estadoPago = "PENDIENTE";

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "total_linea", nullable = false)
    private BigDecimal totalLinea;

    @Column(name = "observaciones", nullable = true)
    private String observaciones;

    @Column(name = "estado_preparacion", nullable = true)
    private String estadoPreparacion;

    // ---- RELACIONES ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Producto producto;
}
