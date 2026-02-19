package com.restaurante.resturante.domain.compras;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 100)
    private String nombreComercial;

    @Column(name = "ruc", length = 11, unique = true)
    private String ruc;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Integer estado = 1;

    // Relación OneToMany con ProveedorMetodosPago
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private Set<ProveedorMetodosPago> metodosPago;
}
