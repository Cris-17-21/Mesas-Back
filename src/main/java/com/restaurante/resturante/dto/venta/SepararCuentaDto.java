package com.restaurante.resturante.dto.venta;

import java.util.List;

public record SepararCuentaDto(
                String pedidoOrigenId,
                List<ItemSepararDto> items,
                String nuevaMesaId // Opcional, si se mueven a otra mesa
) {
        public record ItemSepararDto(
                        String detalleId,
                        int cantidad) {
        }
}
