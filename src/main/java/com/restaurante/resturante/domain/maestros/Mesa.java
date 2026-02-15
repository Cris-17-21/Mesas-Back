package com.restaurante.resturante.domain.maestros;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.ventas.Pedido;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "codigo_mesa", nullable = false)
    private String codigoMesa;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private String estado = "LIBRE";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // ---- RELACIONES ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_actual_id")
    @ToString.Exclude
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piso_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Piso piso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "union_principal_id")
    @ToString.Exclude
    @JsonBackReference("principal-secundario")
    private Mesa principal;

    @OneToMany(mappedBy = "principal", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference("principal-secundario")
    private Set<Mesa> secundarias = new HashSet<>();
}
