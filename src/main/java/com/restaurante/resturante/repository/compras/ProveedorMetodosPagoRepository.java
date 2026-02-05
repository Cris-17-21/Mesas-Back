package com.restaurante.resturante.repository.compras;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.compras.ProveedorMetodosPago;
import com.restaurante.resturante.domain.compras.ProveedorMetodosPagoId;

@Repository
public interface ProveedorMetodosPagoRepository extends JpaRepository<ProveedorMetodosPago, ProveedorMetodosPagoId> {
    // Note: The ID is composite (ProveedorMetodosPagoId), so finding by ID requires
    // the key class.
    // However, we mostly need to find by provider.

    List<ProveedorMetodosPago> findByIdProveedor(Integer idProveedor);

    void deleteByIdProveedor(Integer idProveedor);
}
