package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

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

    @Column(name = "codigo_pedido", nullable = false, unique = true)
    private String codigoPedido;

    @Column(name = "tipo_entrega", nullable = false)
    private String tipoEntrega;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "total_productos")
    private BigDecimal totalProductos;

    @Column(name = "descuento_global")
    private BigDecimal descuentoGlobal;

    @Column(name = "total_final")
    private BigDecimal totalFinal;

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
    @JoinColumn(name = "mesa_id", nullable = true)
    @ToString.Exclude
    @JsonBackReference
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    @ToString.Exclude
    @JsonBackReference
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonBackReference
    private Set<PedidoDetalle> pedidoDetalles;

    // ---- METODOS ----
    public boolean estaPagadoCompletamente() {
        return pedidoDetalles.stream()
            .allMatch(d -> d.getCantidadPagada().equals(d.getCantidad()));
    }
}
