package com.restaurante.resturante.domain.compras;

import com.restaurante.resturante.domain.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "tipospago")
public class TiposPago extends Auditable {

    @Id
    @Column(name = "id_tipopago")
    private Integer idTipoPago;

    @Column(name = "tipo_pago", nullable = false, length = 50, unique = true)
    private String tipoPago;
}
