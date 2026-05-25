package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.ventas.FacturacionComprobante;
import com.restaurante.resturante.domain.ventas.Pedido;
import com.restaurante.resturante.domain.ventas.PedidoDetalle;
import com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionResponse;
import com.restaurante.resturante.dto.api_facturacion.ComprobanteFacturacionDetalle;
import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.dto.venta.NotaCreditoRequestDto;
import com.restaurante.resturante.repository.venta.PedidoRepository;
import com.restaurante.resturante.repository.venta.FacturacionComprobanteRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionApiComprobanteService;
import com.restaurante.resturante.service.maestros.jpa.ClienteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FacturacionComprobanteService {

    private final FacturacionComprobanteRepository facturacionRepository;
    private final PedidoRepository pedidoRepository;
    private final FacturacionApiComprobanteService comprobanteApiService;
    private final ClienteService clienteService;

    @Value("${api.facturacion.url}")
    private String apiBaseUrl;

    @Transactional
    public FacturacionComprobanteDto emitirComprobante(FacturaRequestDto dto) {
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        if (pedido.getCliente() == null && dto.rucApellidos() != null && !dto.rucApellidos().isEmpty()) {
            Cliente cliente = clienteService.getOrCreateClienteByDocument(
                    dto.rucApellidos(),
                    dto.razonSocialNombres(),
                    dto.direccion(),
                    pedido.getSucursal().getEmpresa());
            pedido.setCliente(cliente);
            pedido = pedidoRepository.save(pedido);
        }

        LocalDateTime fechaEmision = null;
        if (dto.fechaEmision() != null && !dto.fechaEmision().isBlank()) {
            fechaEmision = LocalDateTime.parse(dto.fechaEmision(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(fechaEmision.toLocalDate(), LocalDate.now()));
            if (daysDiff > 2) {
                throw new IllegalArgumentException(
                        "La fecha de emisión debe estar dentro de ±2 días de la fecha actual");
            }
        }

        String tipoDoc;
        boolean esNotaDeVenta = false;
        if (dto.tipoComprobante().equals("01") || dto.tipoComprobante().equals("FACTURA")) {
            tipoDoc = "01";
            // Validación de longitud RUC para Factura:
            // Primero intentamos el campo del DTO; si no viene, usamos el número del cliente ya asignado al pedido.
            String ruc = dto.rucApellidos();
            if ((ruc == null || ruc.trim().isEmpty()) && pedido.getCliente() != null) {
                ruc = pedido.getCliente().getNumeroDocumento();
            }
            if (ruc == null || ruc.trim().length() != 11) {
                throw new IllegalArgumentException("La Factura requiere un cliente con RUC válido (11 dígitos).");
            }
        } else if (dto.tipoComprobante().equals("03") || dto.tipoComprobante().equals("BOLETA")) {
            tipoDoc = "03";
            // Validación de longitud DNI para Boleta (si se proporciona)
            String dni = dto.rucApellidos();
            if (dni != null && !dni.trim().isEmpty() && !"00000000".equals(dni.trim())) {
                if (dni.trim().length() != 8) {
                    throw new IllegalArgumentException("La Boleta requiere un DNI válido (8 dígitos) o cliente general.");
                }
            }
        } else if (dto.tipoComprobante().equals("02") || dto.tipoComprobante().equals("NOTA_DE_VENTA")) {
            tipoDoc = "02";
            esNotaDeVenta = true;
        } else {
            throw new IllegalArgumentException("Tipo de comprobante no soportado: " + dto.tipoComprobante());
        }

        ComprobanteFacturacionResponse apiResponse = null;
        if (!esNotaDeVenta) {
            // Emisión diferida: Generamos localmente en estado PENDIENTE_ENVIO
            apiResponse = generarComprobantePendienteLocal(pedido, tipoDoc, fechaEmision);
        } else {
            // Para Nota de Venta, generar todo localmente sin llamar a SUNAT
            apiResponse = generarComprobanteNotaDeVentaLocal(pedido, fechaEmision);
        }

        FacturacionComprobante comprobante = mapToEntity(apiResponse, pedido);
        comprobante.setEstadoSunat(esNotaDeVenta ? "ACEPTADO" : "PENDIENTE_ENVIO");
        comprobante = facturacionRepository.save(comprobante);

        // Guardar archivos iniciales (TXT para nota de venta, PDF local si aplica)
        guardarArchivosComprobanteLocal(comprobante, apiResponse);

        return toDto(comprobante, apiResponse);
    }

    @Transactional
    public FacturacionComprobanteDto emitirNotaCredito(NotaCreditoRequestDto dto) {
        FacturacionComprobante ref = facturacionRepository.findById(dto.comprobanteId())
                .orElseThrow(() -> new RuntimeException("COMPROBANTE DE REFERENCIA NO ENCONTRADO"));

        Pedido pedido = ref.getPedido();
        String tipoDocAfectado = ref.getTipoComprobante();
        String numDocAfectado = ref.getSerie() + "-" + String.format("%08d",
                Integer.parseInt(ref.getCorrelativo()));

        // Emitimos la Nota de Crédito directamente
        ComprobanteFacturacionResponse apiResponse = comprobanteApiService.emitirNotaCredito(
                pedido, tipoDocAfectado, numDocAfectado,
                dto.codMotivo(), dto.descripcion());

        FacturacionComprobante nc = mapToEntity(apiResponse, pedido);
        nc.setComprobanteReferencia(ref);
        nc.setCodMotivoNota(dto.codMotivo());
        nc.setDescripcionMotivo(dto.descripcion());
        nc = facturacionRepository.save(nc);

        // Guardamos los archivos electrónicos de la Nota de Crédito
        guardarArchivosComprobanteLocal(nc, apiResponse);

        return toDto(nc, apiResponse);
    }

    @Transactional
    public void procesarEnviosDiferidos() {
        log.info("Procesando comprobantes pendientes de envío...");
        List<FacturacionComprobante> pendientes = facturacionRepository.findByEstadoSunat("PENDIENTE_ENVIO");
        LocalDateTime limite = LocalDateTime.now().minusMinutes(30);

        for (FacturacionComprobante c : pendientes) {
            if (c.getFechaEmision().isBefore(limite)) {
                try {
                    log.info("Enviando comprobante diferido ID: {}, Número: {}-{}", c.getId(), c.getSerie(), c.getCorrelativo());
                    ComprobanteFacturacionResponse apiResponse = comprobanteApiService.emitir(
                            c.getPedido(), c.getTipoComprobante(), c.getFechaEmision());
                    
                    c.setEstadoSunat(apiResponse.estadoSunat() != null ? apiResponse.estadoSunat() : "ACEPTADO");
                    c.setHashCpe(apiResponse.cdrHash());
                    c.setArchivoXml(buildDownloadUrl(apiResponse, "xml"));
                    c.setArchivoPdf(buildDownloadUrl(apiResponse, "pdf"));

                    facturacionRepository.save(c);

                    // Descargar y guardar XML y CDR localmente
                    guardarArchivosComprobanteLocal(c, apiResponse);

                    log.info("Comprobante enviado y actualizado con éxito.");
                } catch (Exception e) {
                    log.error("Error al enviar comprobante diferido {}-{}: {}", c.getSerie(), c.getCorrelativo(), e.getMessage());
                    c.setSunatMensajeError(e.getMessage());
                    facturacionRepository.save(c);
                }
            }
        }
    }

    @Transactional
    public FacturacionComprobanteDto enviarComprobanteManual(String id) {
        FacturacionComprobante c = facturacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("COMPROBANTE NO ENCONTRADO"));

        if (!"PENDIENTE_ENVIO".equals(c.getEstadoSunat())) {
            throw new IllegalStateException("El comprobante ya fue enviado a SUNAT o no está en estado pendiente.");
        }

        log.info("Enviando de forma manual e inmediata el comprobante ID: {}-{}.", c.getSerie(), c.getCorrelativo());
        ComprobanteFacturacionResponse apiResponse = comprobanteApiService.emitir(
                c.getPedido(), c.getTipoComprobante(), c.getFechaEmision());

        c.setEstadoSunat(apiResponse.estadoSunat() != null ? apiResponse.estadoSunat() : "ACEPTADO");
        c.setHashCpe(apiResponse.cdrHash());
        c.setArchivoXml(buildDownloadUrl(apiResponse, "xml"));
        c.setArchivoPdf(buildDownloadUrl(apiResponse, "pdf"));

        c = facturacionRepository.save(c);

        guardarArchivosComprobanteLocal(c, apiResponse);

        return toDto(c, apiResponse);
    }

    @Transactional
    public void eliminarComprobantePendiente(String id) {
        FacturacionComprobante c = facturacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("COMPROBANTE NO ENCONTRADO"));

        if (!"PENDIENTE_ENVIO".equals(c.getEstadoSunat())) {
            throw new IllegalStateException("No se puede eliminar un comprobante que ya fue enviado o procesado.");
        }

        facturacionRepository.delete(c);
        log.info("Comprobante pendiente ID {} eliminado físicamente de la base de datos.", id);
    }

    @Transactional(readOnly = true)
    public List<FacturacionComprobanteDto> buscarComprobantesConFiltros(
            String sucursalId, String tipo, String serie, String correlativo, String fechaInicio, String fechaFin) {
        
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (fechaInicio != null && !fechaInicio.isBlank()) {
            inicio = LocalDateTime.parse(fechaInicio + " 00:00:00", formatter);
        }
        if (fechaFin != null && !fechaFin.isBlank()) {
            fin = LocalDateTime.parse(fechaFin + " 23:59:59", formatter);
        }

        return facturacionRepository.buscarComprobantes(
                sucursalId, 
                (tipo == null || tipo.isBlank()) ? null : tipo, 
                (serie == null || serie.isBlank()) ? null : serie, 
                (correlativo == null || correlativo.isBlank()) ? null : correlativo, 
                inicio, 
                fin
        ).stream()
         .map(c -> new FacturacionComprobanteDto(
                 c.getId(), c.getTipoComprobante(), c.getSerie(), c.getCorrelativo(),
                 c.getRucEmisor(), c.getFechaEmision() != null ? c.getFechaEmision().toString() : null,
                 c.getTotalVenta(),
                 c.getPedido() != null ? c.getPedido().getId() : null,
                 c.getArchivoXml(), c.getArchivoPdf(),
                 c.getArchivoTxt()))
         .toList();
    }

    private ComprobanteFacturacionResponse generarComprobantePendienteLocal(Pedido pedido, String tipoDoc, LocalDateTime fechaEmision) {
        LocalDateTime fechaUsar = fechaEmision != null ? fechaEmision : LocalDateTime.now();
        String serie = "01".equals(tipoDoc) ? "F001" : "B001";
        
        Integer maxCorr = facturacionRepository.obtenerMaxCorrelativo(pedido.getSucursal().getId(), tipoDoc, serie);
        int siguiente = (maxCorr != null ? maxCorr : 0) + 1;

        BigDecimal total = pedido.getTotalFinal();
        BigDecimal mtoOperGravadas = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal mtoIgv = total.subtract(mtoOperGravadas);

        return new ComprobanteFacturacionResponse(
                null, // id
                tipoDoc,
                serie,
                siguiente,
                serie + "-" + String.format("%08d", siguiente),
                fechaUsar.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "PEN",
                mtoOperGravadas,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                mtoIgv,
                total,
                "PENDIENTE_ENVIO",
                null, // ticketSunat
                null, // cdrCodigo
                null, // cdrDescripcion
                null, // cdrHash
                null  // archivo
        );
    }

    private ComprobanteFacturacionResponse generarComprobanteNotaDeVentaLocal(Pedido pedido, LocalDateTime fechaEmision) {
        LocalDateTime fechaUsar = fechaEmision != null ? fechaEmision : LocalDateTime.now();
        String serie = "F002";
        
        Integer maxCorr = facturacionRepository.obtenerMaxCorrelativo(pedido.getSucursal().getId(), "02", serie);
        int siguiente = (maxCorr != null ? maxCorr : 0) + 1;

        BigDecimal total = pedido.getTotalFinal();
        BigDecimal mtoOperGravadas = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal mtoIgv = total.subtract(mtoOperGravadas);

        return new ComprobanteFacturacionResponse(
                null, // id
                "02",
                serie,
                siguiente,
                serie + "-" + String.format("%08d", siguiente),
                fechaUsar.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "PEN",
                mtoOperGravadas,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                mtoIgv,
                total,
                "ACEPTADO",
                null,
                null,
                null,
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public java.util.List<FacturacionComprobanteDto> listarComprobantes(String sucursalId) {
        return facturacionRepository.findBySucursalIdOrderByFechaEmisionDesc(sucursalId).stream()
                .map(c -> new FacturacionComprobanteDto(
                        c.getId(), c.getTipoComprobante(), c.getSerie(), c.getCorrelativo(),
                        c.getRucEmisor(), c.getFechaEmision() != null ? c.getFechaEmision().toString() : null,
                        c.getTotalVenta(),
                        c.getPedido() != null ? c.getPedido().getId() : null,
                        c.getArchivoXml(), c.getArchivoPdf(),
                        c.getArchivoTxt()))
                .toList();
    }

    private FacturacionComprobante mapToEntity(ComprobanteFacturacionResponse api, Pedido pedido) {
        return FacturacionComprobante.builder()
                .pedido(pedido)
                .cliente(pedido.getCliente())
                .tipoComprobante(api.tipoDoc())
                .serie(api.serie())
                .correlativo(String.format("%08d", api.correlativo()))
                .fechaEmision(api.fechaEmision() != null ? LocalDateTime.parse(api.fechaEmision()) : LocalDateTime.now())
                .rucEmisor(pedido.getSucursal().getEmpresa().getRuc())
                .totalGravadas(api.mtoOperGravadas() != null ? api.mtoOperGravadas() : BigDecimal.ZERO)
                .totalExoneradas(api.mtoOperExoneradas() != null ? api.mtoOperExoneradas() : BigDecimal.ZERO)
                .totalInafectas(api.mtoOperInafectas() != null ? api.mtoOperInafectas() : BigDecimal.ZERO)
                .montoIgv(api.mtoIgv() != null ? api.mtoIgv() : BigDecimal.ZERO)
                .montoTotal(api.mtoImpVenta() != null ? api.mtoImpVenta() : BigDecimal.ZERO)
                .totalVenta(api.mtoImpVenta() != null ? api.mtoImpVenta() : BigDecimal.ZERO)
                .estadoSunat(api.estadoSunat())
                .hashCpe(api.cdrHash())
                .archivoXml(buildDownloadUrl(api, "xml"))
                .archivoPdf(buildDownloadUrl(api, "pdf"))
                .empresa(pedido.getSucursal().getEmpresa())
                .sucursal(pedido.getSucursal())
                .cajaTurno(pedido.getCajaTurno()) // Asociamos la caja de forma directa
                .build();
    }

    private void guardarArchivosComprobanteLocal(FacturacionComprobante comprobante, ComprobanteFacturacionResponse apiResponse) {
        try {
            java.nio.file.Path rootPath = java.nio.file.Paths.get("var", "comprobantes");
            String anio = String.valueOf(comprobante.getFechaEmision().getYear());
            String mes = String.format("%02d", comprobante.getFechaEmision().getMonthValue());
            String tipo = comprobante.getTipoComprobante();

            java.nio.file.Path dir = rootPath.resolve(anio).resolve(mes).resolve(tipo);
            java.nio.file.Files.createDirectories(dir);

            String baseName = comprobante.getSerie() + "-" + comprobante.getCorrelativo();

            // 1. Guardamos el XML
            if (comprobante.getArchivoXml() != null) {
                descargarYGuardarArchivo(comprobante.getArchivoXml(), dir.resolve(baseName + ".xml"));
            }

            // 2. Guardamos el PDF
            if (comprobante.getArchivoPdf() != null) {
                descargarYGuardarArchivo(comprobante.getArchivoPdf(), dir.resolve(baseName + ".pdf"));
            }

            // 3. Guardamos el CDR (Si está disponible)
            String cdrUrl = buildDownloadUrl(apiResponse, "cdr");
            if (cdrUrl != null) {
                descargarYGuardarArchivo(cdrUrl, dir.resolve(baseName + "-cdr.xml"));
            }

            // 4. Generamos y guardamos el TXT local
            String txtContent = generarTxtNotaDeVenta(comprobante.getPedido(), apiResponse);
            java.nio.file.Files.writeString(dir.resolve(baseName + ".txt"), txtContent);
            comprobante.setArchivoTxt(txtContent);

        } catch (Exception e) {
            log.error("Error al almacenar archivos locales del comprobante: {}", e.getMessage());
        }
    }

    private void descargarYGuardarArchivo(String urlString, java.nio.file.Path destinoPath) {
        if (urlString == null || urlString.isEmpty()) return;
        try {
            java.net.URL url = new java.net.URL(urlString);
            try (java.io.InputStream in = url.openStream()) {
                java.nio.file.Files.createDirectories(destinoPath.getParent());
                java.nio.file.Files.copy(in, destinoPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("Fallo al descargar y guardar archivo en {}: {}", destinoPath, e.getMessage());
        }
    }

    private String buildDownloadUrl(ComprobanteFacturacionResponse api, String tipo) {
        if (api == null || api.archivo() == null)
            return null;
        String token = switch (tipo) {
            case "xml" -> api.archivo().xmlToken();
            case "pdf" -> api.archivo().pdfToken();
            case "cdr" -> api.archivo().cdrToken();
            default -> null;
        };
        if (token == null)
            return null;
        String formato = tipo.equals("pdf") ? "&formato=ticket" : "";
        return apiBaseUrl + "/api/v1/descargar/" + token + "?tipo=" + tipo + formato;
    }

    private String generarTxtNotaDeVenta(Pedido pedido, ComprobanteFacturacionResponse apiResponse) {
        StringBuilder txt = new StringBuilder();
        
        txt.append("COMPROBANTE ELECTRÓNICO\n");
        txt.append("=====================\n\n");
        
        Empresa empresa = pedido.getSucursal().getEmpresa();
        txt.append("EMISOR:\n");
        txt.append("RUC: ").append(empresa.getRuc()).append("\n");
        txt.append("Razón Social: ").append(empresa.getRazonSocial() != null ? empresa.getRazonSocial() : "").append("\n");
        txt.append("Dirección: ").append(empresa.getDireccionFiscal() != null ? empresa.getDireccionFiscal() : "").append("\n\n");
        
        txt.append("COMPROBANTE:\n");
        txt.append("Tipo: ").append(apiResponse.tipoDoc()).append("\n");
        txt.append("Serie: ").append(apiResponse.serie()).append("\n");
        txt.append("Número: ").append(String.format("%08d", apiResponse.correlativo())).append("\n");
        txt.append("Fecha de emisión: ").append(apiResponse.fechaEmision() != null ? apiResponse.fechaEmision() : LocalDateTime.now()).append("\n");
        txt.append("Moneda: PEN\n\n");
        
        Cliente cliente = pedido.getCliente();
        if (cliente != null) {
            txt.append("CLIENTE:\n");
            txt.append("Tipo doc: ").append(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().getName() : "").append("\n");
            txt.append("Número doc: ").append(cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "").append("\n");
            txt.append("Razón Social: ").append(cliente.getNombreRazonSocial() != null ? cliente.getNombreRazonSocial() : "").append("\n");
            if (cliente.getDireccion() != null) {
                txt.append("Dirección: ").append(cliente.getDireccion()).append("\n");
            }
            txt.append("\n");
        } else {
            txt.append("CLIENTE: Cliente General\n\n");
        }
        
        txt.append("DETALLES:\n");
        txt.append("Cant. Descripción                  Valor Unit.  Importe\n");
        txt.append("--------------------------------------------------------\n");
        
        BigDecimal totalVenta = BigDecimal.ZERO;
        List<PedidoDetalle> detalles = pedido.getPedidoDetalles();
        if (detalles != null) {
            for (PedidoDetalle detalle : detalles) {
                String descripcion = detalle.getProducto() != null ? detalle.getProducto().getNombreProducto() : "Producto";
                if (descripcion.length() > 30) {
                    descripcion = descripcion.substring(0, 30);
                }
                txt.append(String.format("%-4s %-30s %12s %12s\n", 
                    detalle.getCantidad(),
                    descripcion,
                    detalle.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP),
                    detalle.getTotalLinea().setScale(2, RoundingMode.HALF_UP)));
                
                totalVenta = totalVenta.add(detalle.getTotalLinea());
            }
        }
        
        txt.append("--------------------------------------------------------\n");
        txt.append(String.format("%37s %12s\n", "Sub Total:", totalVenta.setScale(2, RoundingMode.HALF_UP)));
        
        BigDecimal totalIgv = totalVenta.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        txt.append(String.format("%37s %12s\n", "IGV (18%):", totalIgv));
        
        txt.append(String.format("%37s %12s\n", "TOTAL:", totalVenta.add(totalIgv).setScale(2, RoundingMode.HALF_UP)));
        
        txt.append("\n");
        txt.append("Leyenda: Comprobante electrónico generado en sistema interno.\n");
        
        return txt.toString();
    }

    private FacturacionComprobanteDto toDto(FacturacionComprobante c, ComprobanteFacturacionResponse api) {
        return new FacturacionComprobanteDto(
                c.getId(), c.getTipoComprobante(), c.getSerie(), c.getCorrelativo(),
                c.getRucEmisor(), c.getFechaEmision() != null ? c.getFechaEmision().toString() : null,
                c.getTotalVenta(),
                c.getPedido() != null ? c.getPedido().getId() : null,
                c.getArchivoXml(), c.getArchivoPdf(),
                c.getArchivoTxt());
    }
}
