package com.restaurante.resturante.domain.compras;

import java.io.Serializable;
import java.util.Objects;

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
public class ProveedorMetodosPagoId implements Serializable {
    private Integer idProveedor;
    private Integer idTipoPago;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProveedorMetodosPagoId that = (ProveedorMetodosPagoId) o;
        return Objects.equals(idProveedor, that.idProveedor) &&
                Objects.equals(idTipoPago, that.idTipoPago);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProveedor, idTipoPago);
    }
}
