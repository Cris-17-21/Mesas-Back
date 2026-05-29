package com.restaurante.resturante.repository.sincronizacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.restaurante.resturante.domain.sincronizacion.RegistroSincronizacion;

@Repository
public interface RegistroSincronizacionRepository extends JpaRepository<RegistroSincronizacion, String> {
    List<RegistroSincronizacion> findByEstado(String estado);
}
