package com.restaurante.resturante.domain.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.restaurante.resturante.domain.maestros.Cliente;
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
@Table(name = "facturacion_comprobantes")
public class FacturacionComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "tipo_comprobante", nullable = false)
    private String tipoComprobante; // 01=Factura, 03=Boleta, 07=NC, etc.

    @Column(name = "serie", nullable = false)
    private String serie;

    @Column(name = "correlativo", nullable = false)
    private String correlativo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    // Campos de Importes
    @Column(name = "total_gravadas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalGravadas = BigDecimal.ZERO;

    @Column(name = "total_exoneradas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalExoneradas = BigDecimal.ZERO;

    @Column(name = "total_inafectas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalInafectas = BigDecimal.ZERO;

    @Column(name = "total_gratuitas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalGratuitas = BigDecimal.ZERO;

    @Column(name = "monto_igv", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoIgv = BigDecimal.ZERO;

    @Column(name = "total_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVenta;

    @Column(name = "ruc_emisor", length = 11)
    private String rucEmisor;

    // Estado SUNAT
    @Column(name = "hash_cpe")
    private String hashCpe;

    @Column(name = "estado_sunat")
    private String estadoSunat; // PENDIENTE, ENVIADO, ACEPTADO, RECHAZADO, ANULADO

    @Column(name = "sunat_mensaje_error", columnDefinition = "TEXT")
    private String sunatMensajeError;

    @Column(name = "cdr_sunat_xml", columnDefinition = "TEXT")
    private String cdrSunatXml; // Respuesta XML de SUNAT (Base64 o texto)

    @Column(name = "archivo_pdf")
    private String archivoPdf;

    @Column(name = "archivo_xml")
    private String archivoXml;

    // Nota de Crédito/Débito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_referencia_id")
    @ToString.Exclude
    private FacturacionComprobante comprobanteReferencia;

    @Column(name = "cod_motivo_nota")
    private String codMotivoNota;

    @Column(name = "descripcion_motivo")
    private String descripcionMotivo;

    // RELACIONES
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @ToString.Exclude
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    @ToString.Exclude
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_turno_id") // Opcional, pero bueno para rastreo
    @ToString.Exclude
    private CajaTurno cajaTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    private Cliente cliente;

}
