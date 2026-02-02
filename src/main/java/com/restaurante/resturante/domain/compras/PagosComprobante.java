package com.restaurante.resturante.domain.compras;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pagoscomprobante")
public class PagosComprobante {

    @Id
    @Column(name = "id_pago")
    private Long idPago;

    // Asumimos que id_comprobante se refiere al PedidoCompra por el diagrama
    // Si fuera otro comprobante (e.g. Factura Externa), no tendríamos la entidad.
    // Lo mapearemos como ID directo por si acaso, o a PedidoCompra si el usuario
    // confirma.
    // Por ahora, siguiendo SQL literal: BIGINT
    @Column(name = "id_comprobante", nullable = false)
    private Long idComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipopago", nullable = false)
    private TiposPago tipoPago;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;
}
