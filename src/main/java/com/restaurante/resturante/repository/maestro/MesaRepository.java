package com.restaurante.resturante.repository.maestro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.restaurante.resturante.domain.maestros.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, String> {

    // Buscar mesas por área/piso (ej: Segundo Piso)
    List<Mesa> findByPisoId(String pisoId);

    // Buscar mesas por estado (ej: Ver todas las Libres)
    List<Mesa> findByEstadoAndActiveTrue(String estado);

    // Para cargar el mapa de mesas por piso
    List<Mesa> findByPisoIdOrderByCodigoMesaAsc(String pisoId);

    // Para la UNIÓN: buscar mesas que comparten el mismo pedido
    List<Mesa> findByPedidoId(@Param("pedidoId") String pedidoId);

    // Para saber si una mesa está libre/ocupada rápido
    Optional<Mesa> findByCodigoMesaAndPisoSucursalId(String codigo, String sucursalId);

    List<Mesa> findByPrincipalId(String principalId); // <--- ESTO ES CLAVE
}
