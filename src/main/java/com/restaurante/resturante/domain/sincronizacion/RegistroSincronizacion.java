package com.restaurante.resturante.domain.sincronizacion;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registro_sincronizacion")
@Getter
@Setter
public class RegistroSincronizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String tablaNombre;
    private String registroId;
    private String estado; // PENDIENTE, SINCRONIZADO, ERROR
    private String detalleError;
    private LocalDateTime fechaModificacion;
}
