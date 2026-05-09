package com.restaurante.resturante.domain.maestros;

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
import jakarta.persistence.Lob;
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

    @Column(name = "nombre_comercial", nullable = true)
    private String nombreComercial;

    @Column(name = "direccion_fiscal", nullable = true)
    private String direccionFiscal;

    @Column(name = "ubigeo", length = 6)
    private String ubigeo;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(nullable = true)
    private String telefono;

    @Column(nullable = true, unique = true)
    private String email;

    @Lob
    @Column(nullable = true, columnDefinition = "LONGBLOB")
    private byte[] logoUrl;

    @Column(nullable = false)
    private LocalDate fechaAfiliacion;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // Estado por AdminMaster
    @Column(name = "shadow_ban")
    @Builder.Default
    private Boolean shadowBan = false;

    // Propiedades para Sunat
    @Column(name = "usuario_sol")
    private String usuarioSol;

    @Column(name = "clave_sol")
    private String claveSol;

    @Column(name = "clave_certificado")
    private String claveCertificado;

    @Column(name = "entorno")
    private Boolean entorno; // true = producción, false = desarrollo

    @Column(name = "certificado_digital", columnDefinition = "TEXT")
    private String certificadoDigital;

    // ---- RELACIONES ----
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<Sucursal> sucursales;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<UserAccess> usersAccess;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<Cliente> clientes;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonBackReference
    private Set<MedioPago> mediosPago;
}
