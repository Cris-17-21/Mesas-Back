package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.domain.maestros.Mesa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "codigo_pedido", nullable = false)
    private String codigoPedido;

    @Column(name = "tipo_entrega", nullable = false)
    @Builder.Default
    private String tipoEntrega = "MESA"; // MESA, LLEVAR, DELIVERY

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private String estado = "ABIERTO"; // ABIERTO, PAGADO, ANULADO

    @Column(name = "total_productos")
    @Builder.Default
    private BigDecimal totalProductos = BigDecimal.ZERO;

    @Column(name = "descuento_global")
    @Builder.Default
    private BigDecimal descuentoGlobal = BigDecimal.ZERO;

    @Column(name = "total_final")
    @Builder.Default
    private BigDecimal totalFinal = BigDecimal.ZERO;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // ---- RELACIONES ----
    // FALTA RELACION CON SUCURSAL, MESA, USUARIO Y CLIENTE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_turno_id", nullable = false) // <--- AGREGADO: Crucial para arqueo
    @ToString.Exclude
    private CajaTurno cajaTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    @JsonBackReference
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonBackReference
    private List<PedidoDetalle> pedidoDetalles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    @ToString.Exclude
    @JsonBackReference
    private Mesa mesa;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonBackReference
    private List<PedidoPago> pagos;

    // ---- METODOS ----
    // Este método te ayudará mucho en el Service para recalcular totales
    public void calcularTotales() {
        this.totalProductos = pedidoDetalles.stream()
                .map(PedidoDetalle::getTotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalFinal = this.totalProductos.subtract(this.descuentoGlobal);
    }

    public boolean estaPagadoCompletamente() {
        if (pedidoDetalles == null || pedidoDetalles.isEmpty())
            return false;
        return pedidoDetalles.stream()
                .allMatch(d -> d.getCantidadPagada() >= d.getCantidad());
    }

    public BigDecimal getMontoPagado() {
        if (pagos == null)
            return BigDecimal.ZERO;
        return pagos.stream()
                .map(PedidoPago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Para saber si ya cubrieron el total
    public boolean estaTotalmentePagado() {
        return getMontoPagado().compareTo(this.totalFinal) >= 0;
    }
}
