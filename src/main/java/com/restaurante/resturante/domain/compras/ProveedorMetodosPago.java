package com.restaurante.resturante.domain.compras;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

// Clave compuesta para JPA
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProveedorMetodosPagoId implements Serializable {
    private Integer idProveedor;
    private Integer idTipoPago;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "proveedor_metodospago")
@IdClass(ProveedorMetodosPagoId.class)
public class ProveedorMetodosPago {

    @Id
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Id
    @Column(name = "id_tipopago")
    private Integer idTipoPago;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", insertable = false, updatable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "id_tipopago", insertable = false, updatable = false)
    private TiposPago tiposPago;

    @Column(name = "datos_pago", nullable = false)
    private String datosPago;
}
