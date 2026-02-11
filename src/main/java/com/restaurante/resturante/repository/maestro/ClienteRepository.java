package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, String>{

    // Buscar todos los clientes activos de una empresa específica
    List<Cliente> findByEmpresaIdAndActiveTrue(String empresaId);

    // Buscar un cliente por su número de documento (DNI/RUC)
    // Útil para el autocompletado en el sistema de ventas
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    // Buscar clientes por nombre o razón social (Búsqueda predictiva)
    List<Cliente> findByNombreRazonSocialContainingIgnoreCaseAndActiveTrue(String nombre);

    // Verificar si ya existe un documento registrado en la empresa
    boolean existsByNumeroDocumentoAndEmpresaId(String numeroDocumento, String empresaId);
}
