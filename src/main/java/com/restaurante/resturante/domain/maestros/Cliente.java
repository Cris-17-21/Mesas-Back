package com.restaurante.resturante.domain.maestros;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.security.TipoDocumento;
import com.restaurante.resturante.domain.ventas.Pedido;

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
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "nombre_razon_social", nullable = false)
    private String nombreRazonSocial;

    @Column(name = "direccion", nullable = true)
    private String direccion;

    @Column(name = "correo", nullable = true)
    private String correo;

    @Column(name = "telefono", nullable = true)
    private String telefono;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // ---- RELACIONES ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private TipoDocumento tipoDocumento;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<Pedido> pedidos;
}
