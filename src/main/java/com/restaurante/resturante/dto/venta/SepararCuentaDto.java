package com.restaurante.resturante.dto.venta;

import java.util.List;

public record SepararCuentaDto(
        String pedidoOrigenId,
        List<String> detallesIds,
        String nuevaMesaId // Opcional, si se mueven a otra mesa
) {
}
