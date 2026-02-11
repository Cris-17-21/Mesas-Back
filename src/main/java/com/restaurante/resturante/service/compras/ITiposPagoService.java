package com.restaurante.resturante.service.compras;

import java.util.List;
import com.restaurante.resturante.dto.compras.TiposPagoDto;

public interface ITiposPagoService {
    List<TiposPagoDto> findAll();
}
