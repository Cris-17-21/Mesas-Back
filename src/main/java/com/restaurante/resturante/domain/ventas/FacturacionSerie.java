package com.restaurante.resturante.domain.ventas;

import java.time.LocalDateTime;

import com.restaurante.resturante.domain.maestros.Empresa;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Almacena localmente las series de comprobantes configuradas por sucursal.
 * Espeja los datos del facturador externo para operación offline y auditoría.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facturacion_series", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "sucursal_id", "tipo_comprobante", "serie" })
})
public class FacturacionSerie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    /** Código SUNAT: 01=Factura, 03=Boleta, 07=NC, 08=ND */
    @Column(name = "tipo_comprobante", nullable = false, length = 10)
    private String tipoComprobante;

    /** Serie, ej: B001, F002 */
    @Column(name = "serie", nullable = false, length = 10)
    private String serie;

    /** Próximo correlativo a usar */
    @Column(name = "proximo_correlativo", nullable = false)
    private Integer proximoCorrelativo;

    /** ID de la serie en la API del facturador externo (para actualizaciones) */
    @Column(name = "api_serie_id", length = 36)
    private String apiSerieId;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @ToString.Exclude
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    private Empresa empresa;
}
