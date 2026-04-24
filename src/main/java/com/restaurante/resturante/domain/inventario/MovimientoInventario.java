package com.restaurante.resturante.domain.inventario;

import java.time.LocalDateTime;

import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.maestros.Sucursal;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento; // ENTRADA, SALIDA

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "motivo", nullable = false, length = 100)
    private String motivo; // COMPRA, MERMA, PEDIDO, INVENTARIO_INICIAL, USO_INTERNO

    @Column(name = "fecha_movimiento", nullable = false)
    @Builder.Default
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @Column(name = "usuario_id", length = 36)
    private String usuarioId;

    @Column(name = "comprobante", length = 100)
    private String comprobante;
}
