package com.restaurante.resturante.domain.maestros;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.audit.Auditable;

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
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medios_pago")
public class MedioPago extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String nombre; // Ej: "Yape", "Visa", "Efectivo", "Plin"

    // IMPORTANTE: Para saber si suma en el arqueo de caja física
    @Column(name = "es_efectivo", nullable = false)
    private boolean esEfectivo; 

    // IMPORTANTE: Para Facturación Electrónica (Catálogo 59)
    // Efectivo = "009", Tarjetas/Yape = "001"
    @Column(name = "codigo_sunat") 
    private String codigoSunat; 

    @Column(name = "requiere_referencia")
    private boolean requiereReferencia; // True para pedir Nro Operación

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Empresa empresa;
}
