package com.restaurante.resturante.domain.maestros;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.security.UserAccess;

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
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "empresas")
public class Empresa extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, unique = true)
    private String ruc;
    
    @Column(name = "razon_social", nullable = false, unique = true)
    private String razonSocial;

    @Column(name = "direccion_fiscal", nullable = true)
    private String direccionFiscal;

    @Column(nullable = true)
    private String telefono;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = true, unique = true)
    private String logoUrl;

    @Column(nullable = false)
    private LocalDate fechaAfiliacion;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // Estado por AdminMaster
    @Column(name = "shadow_ban")
    @Builder.Default
    private Boolean shadowBan = false;

    // Pagos
    @Column(name = "precio_mensual", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioMensual;

    // ---- RELACIONES ----
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<Sucursal> sucursales;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<HistorialPago> historialPagos;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<UserAccess> usersAccess;
}
