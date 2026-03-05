package com.restaurante.resturante.service.venta;

import java.util.List;

import com.restaurante.resturante.dto.maestro.UnionMesaRequest;
import com.restaurante.resturante.dto.venta.PedidoDetalleRequestDto;
import com.restaurante.resturante.dto.venta.PedidoRequestDto;
import com.restaurante.resturante.dto.venta.PedidoResponseDto;
import com.restaurante.resturante.dto.venta.PedidoResumenDto;
import com.restaurante.resturante.dto.venta.PreCuentaDto;
import com.restaurante.resturante.dto.venta.RegistrarPagoDto;
import com.restaurante.resturante.dto.venta.SepararCuentaDto;

public interface IPedidoService {

    // Gestión principal
    PedidoResponseDto crearPedido(PedidoRequestDto dto);

    PedidoResponseDto obtenerPorId(String id);

    List<PedidoResumenDto> listarPedidosActivos(String sucursalId);

    List<PedidoResumenDto> listarPedidosPorTipo(String sucursalId, String tipoEntrega);

    // Gestión de comanda (agregar platos a una mesa ya abierta)
    PedidoResponseDto actualizarDetalles(String pedidoId, List<PedidoDetalleRequestDto> nuevosDetalles);

    // Acciones de mesa desde el flujo de venta
    void unirMesas(UnionMesaRequest dto);

    // Cierre de cuenta y flujo de caja
    void registrarPago(RegistrarPagoDto dto);

    // Separar cuentas (Split Bill)
    PedidoResponseDto separarCuenta(SepararCuentaDto dto);

    // Generar Pre-cuenta (Vista previa para el cliente)
    PreCuentaDto generarPreCuenta(String pedidoId);
}
