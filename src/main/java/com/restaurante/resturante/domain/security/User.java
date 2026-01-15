package com.restaurante.resturante.domain.security;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.restaurante.resturante.domain.audit.Auditable;
import com.restaurante.resturante.domain.security.enums.Sexo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
public class User extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false)
    private String apellidoMaterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private TipoDocumento tipoDocumento;

    @Column(name="numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // ------METODO DE SPRING SECURITY------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (this.role == null) {
            return Collections.emptyList();
        }

        Stream<GrantedAuthority> roleAuthorities = Stream.of(
                new SimpleGrantedAuthority(role.getName()));

        Stream<GrantedAuthority> permissionAuthorities = role.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()));

        return Stream.concat(roleAuthorities, permissionAuthorities)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // La cuenta nunca caduca
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // La contraseña nunca caduca
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

    // ------FIN METODO DE SPRING SECURITY------
}
