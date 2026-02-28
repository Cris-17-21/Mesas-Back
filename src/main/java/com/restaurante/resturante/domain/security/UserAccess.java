package com.restaurante.resturante.domain.security;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "users_access")
public class UserAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    @ToString.Exclude
    @JsonBackReference
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    @ToString.Exclude
    @JsonBackReference
    private Sucursal sucursal;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;
}
