package com.restaurante.resturante.domain.inventario;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombreProducto;

    @Column(name = "descripcion", length = 600)
    private String descripcion;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "costo_compra", precision = 10, scale = 2)
    private BigDecimal costoCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaProducto categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "peso_gramos")
    private Integer pesoGramos;

    @Column(name = "estado", nullable = false)
    private Boolean estado;

    @Column(name = "imagen")
    private String imagen;


    // ---- RELACIONES ----
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "producto_tipos", joinColumns = @JoinColumn(name = "id_producto"), inverseJoinColumns = @JoinColumn(name = "id_tipo"))
    private Set<TiposProducto> tipos;

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<PedidoDetalle> pedidoDetalles;
}
