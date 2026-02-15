package com.restaurante.resturante.domain.compras;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.security.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "pedidoscompra")
public class PedidoCompra extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido_compra")
    private Long idPedidoCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    // Use UUID string for User since User entity uses String ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User usuario;

    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(name = "fecha_entrega_esperada")
    private LocalDate fechaEntregaEsperada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipopago")
    private TiposPago tipoPago;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado_pedido", nullable = false, length = 50)
    @Builder.Default
    private String estadoPedido = "Pendiente";

    @Column(name = "total_pedido", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPedido = BigDecimal.ZERO;

    @Column(name = "aplica_igv", nullable = false)
    @Builder.Default
    private Boolean aplicaIgv = true;

    @PrePersist
    public void prePersist() {
        if (fechaPedido == null) {
            fechaPedido = LocalDateTime.now();
        }
    }
}
