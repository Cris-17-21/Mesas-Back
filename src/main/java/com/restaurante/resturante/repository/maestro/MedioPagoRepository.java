package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.resturante.domain.maestros.MedioPago;

public interface MedioPagoRepository extends JpaRepository<MedioPago, String>{
    // Listar solo los activos de MI empresa
    List<MedioPago> findByEmpresaIdAndIsActiveTrue(String empresaId);

    // Buscar uno específico (seguridad para que no editen uno de otra empresa)
    Optional<MedioPago> findByIdAndEmpresaIdAndIsActiveTrue(String id, String empresaId);

    // Evitar duplicados de nombre en la misma empresa
    boolean existsByNombreAndEmpresaIdAndIsActiveTrue(String nombre, String empresaId);
}
