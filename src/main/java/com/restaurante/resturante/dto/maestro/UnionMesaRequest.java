package com.restaurante.resturante.dto.maestro;

import java.util.List;

public record UnionMesaRequest(
    String idPrincipal,
    List<String> idsSecundarios
) {}
