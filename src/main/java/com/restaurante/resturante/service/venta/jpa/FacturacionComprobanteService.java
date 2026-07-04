package com.restaurante.resturante.service.venta.jpa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.restaurante.resturante.repository.venta.FacturacionSerieRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;
import com.restaurante.resturante.domain.ventas.MovimientoCaja;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
import com.restaurante.resturante.domain.ventas.FacturacionSerie;
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
    private final com.restaurante.resturante.service.api_facturacion.FacturacionSerieService serieApiService;
    private final FacturacionSerieRepository serieLocalRepository;

    @Autowired
    private com.restaurante.resturante.repository.maestro.SucursalRepository sucursalRepository;

    @Autowired
    private com.restaurante.resturante.service.maestros.IMesaService mesaService;

    @Autowired
    private com.restaurante.resturante.repository.venta.PedidoPagoRepository pagoRepository;

    @Autowired
    private MovimientoCajaRepository movimientoRepository;

    @Value("${api.facturacion.url}")
    private String apiBaseUrl;

    @Transactional
    public FacturacionComprobanteDto emitirComprobante(FacturaRequestDto dto) {
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new RuntimeException("PEDIDO NO ENCONTRADO"));

        String docCliente = dto.rucApellidos();
        if (docCliente == null || docCliente.trim().isEmpty()) {
            if (pedido.getCliente() != null) {
                docCliente = pedido.getCliente().getNumeroDocumento();
            } else {
                docCliente = "00000000";
            }
        }

        if (pedido.getCliente() == null || !docCliente.equals(pedido.getCliente().getNumeroDocumento())) {
            Cliente cliente = clienteService.getOrCreateClienteByDocument(
                    docCliente,
                    (dto.razonSocialNombres() != null && !dto.razonSocialNombres().trim().isEmpty()) ? dto.razonSocialNombres() : "CLIENTES VARIOS",
                    dto.direccion(),
                    pedido.getSucursal().getEmpresa());
            pedido.setCliente(cliente);
            pedido = pedidoRepository.save(pedido);
        }

        LocalDateTime fechaEmision = null;
        if (dto.fechaEmision() != null && !dto.fechaEmision().isBlank()) {
            fechaEmision = LocalDateTime.parse(dto.fechaEmision(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDate emisionDate = fechaEmision.toLocalDate();
            LocalDate today = LocalDate.now();
            if (emisionDate.isAfter(today)) {
                throw new IllegalArgumentException("La fecha de emisión no puede ser una fecha futura.");
            }
            long daysDiff = ChronoUnit.DAYS.between(emisionDate, today);
            if (daysDiff > 2) {
                throw new IllegalArgumentException("La fecha de emisión no puede tener más de 2 días de antigüedad.");
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
            // Emisión diferida vía API (pendienteEnvio = true)
            try {
                apiResponse = comprobanteApiService.emitir(pedido, tipoDoc, fechaEmision, true, Boolean.TRUE.equals(dto.impresionConsumo()));
            } catch (Exception e) {
                log.warn("Error al emitir comprobante vía API, procediendo con fallback local: {}", e.getMessage(), e);
                apiResponse = generarComprobantePendienteLocal(pedido, tipoDoc, fechaEmision);
            }
        } else {
            // Para Nota de Venta, generar todo localmente sin llamar a SUNAT
            apiResponse = generarComprobanteNotaDeVentaLocal(pedido, fechaEmision);
        }

        FacturacionComprobante comprobante = mapToEntity(apiResponse, pedido, Boolean.TRUE.equals(dto.impresionConsumo()));
        if (comprobante.getEstadoSunat() == null || comprobante.getEstadoSunat().isBlank()) {
            comprobante.setEstadoSunat(esNotaDeVenta ? "ACEPTADO" : "PENDIENTE_ENVIO");
        }
        comprobante = facturacionRepository.save(comprobante);
        registrarMovimientoCajaSiAceptado(comprobante);

        // Guardar archivos iniciales (TXT para nota de venta, PDF local si aplica)
        guardarArchivosComprobanteLocal(comprobante, apiResponse);

        if (pedido.getMesa() != null) {
            mesaService.cambiarEstado(pedido.getMesa().getId(), "LIBRE");
        }
        if (!"PAGADO".equals(pedido.getEstado())) {
            pedido.setEstado("PAGADO");
            pedido.setFechaCierre(LocalDateTime.now());
            pedidoRepository.save(pedido);
        }

        return toDto(comprobante);
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

        FacturacionComprobante nc = mapToEntity(apiResponse, pedido, false);
        nc.setComprobanteReferencia(ref);
        nc.setCodMotivoNota(dto.codMotivo());
        nc.setDescripcionMotivo(dto.descripcion());
        nc = facturacionRepository.save(nc);

        // Guardamos los archivos electrónicos de la Nota de Crédito
        guardarArchivosComprobanteLocal(nc, apiResponse);

        return toDto(nc);
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
                    ComprobanteFacturacionResponse apiResponse;
                    if (c.getFacturadorId() != null) {
                        try {
                            comprobanteApiService.ensureCompanyExists(c.getEmpresa());
                            apiResponse = comprobanteApiService.reenviar(c.getFacturadorId(), c.getEmpresa().getApiCompanyId());
                        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                            log.warn("Comprobante con facturadorId {} no encontrado en el facturador durante cron (posible rollback o DB limpia). Re-emitiendo desde cero...", c.getFacturadorId());
                            apiResponse = comprobanteApiService.emitir(
                                    c.getPedido(), c.getTipoComprobante(), c.getFechaEmision(), false, c.getImpresionConsumo());
                        }
                    } else {
                        apiResponse = comprobanteApiService.emitir(
                                c.getPedido(), c.getTipoComprobante(), c.getFechaEmision(), false, c.getImpresionConsumo());
                    }
                    
                    c.setFacturadorId(apiResponse.id());
                    c.setSerie(apiResponse.serie());
                    c.setCorrelativo(String.format("%08d", apiResponse.correlativo()));

                    c.setEstadoSunat(apiResponse.estadoSunat() != null ? apiResponse.estadoSunat() : "ACEPTADO");
                    c.setHashCpe(apiResponse.cdrHash());
                    c.setArchivoXml(buildDownloadUrl(apiResponse, "xml"));
                    c.setArchivoPdf(buildDownloadUrl(apiResponse, "pdf"));

                    facturacionRepository.save(c);
                    registrarMovimientoCajaSiAceptado(c);

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
        ComprobanteFacturacionResponse apiResponse;
        if (c.getFacturadorId() != null) {
            try {
                comprobanteApiService.ensureCompanyExists(c.getEmpresa());
                apiResponse = comprobanteApiService.reenviar(c.getFacturadorId(), c.getEmpresa().getApiCompanyId());
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                log.warn("Comprobante con facturadorId {} no encontrado en el facturador durante envío manual (posible rollback o DB limpia). Re-emitiendo desde cero...", c.getFacturadorId());
                apiResponse = comprobanteApiService.emitir(
                        c.getPedido(), c.getTipoComprobante(), c.getFechaEmision(), false, c.getImpresionConsumo());
            }
        } else {
            apiResponse = comprobanteApiService.emitir(
                    c.getPedido(), c.getTipoComprobante(), c.getFechaEmision(), false, c.getImpresionConsumo());
        }

        c.setFacturadorId(apiResponse.id());
        c.setSerie(apiResponse.serie());
        c.setCorrelativo(String.format("%08d", apiResponse.correlativo()));

        c.setEstadoSunat(apiResponse.estadoSunat() != null ? apiResponse.estadoSunat() : "ACEPTADO");
        c.setHashCpe(apiResponse.cdrHash());
        c.setArchivoXml(buildDownloadUrl(apiResponse, "xml"));
        c.setArchivoPdf(buildDownloadUrl(apiResponse, "pdf"));

        c = facturacionRepository.save(c);
        registrarMovimientoCajaSiAceptado(c);

        guardarArchivosComprobanteLocal(c, apiResponse);

        return toDto(c);
    }

    @Transactional
    public void eliminarComprobantePendiente(String id) {
        FacturacionComprobante c = facturacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("COMPROBANTE NO ENCONTRADO"));

        if (!"PENDIENTE_ENVIO".equals(c.getEstadoSunat()) && !"02".equals(c.getTipoComprobante())) {
            throw new IllegalStateException("Solo se pueden eliminar comprobantes pendientes de envío o notas de venta.");
        }

        Pedido pedido = c.getPedido();
        if (pedido != null) {
            pedido.setEstado("ABIERTO");
            pedido.setFechaCierre(null);
            
            if (pedido.getMesa() != null) {
                mesaService.cambiarEstado(pedido.getMesa().getId(), "OCUPADA");
            }
            
            if (pedido.getPagos() != null) {
                pagoRepository.deleteAll(pedido.getPagos());
                pedido.getPagos().clear();
            }
            pedidoRepository.save(pedido);
        }

        facturacionRepository.delete(c);
        log.info("Comprobante pendiente/NV ID {} eliminado. Pedido {} ha sido reabierto.", id, pedido != null ? pedido.getId() : "N/A");
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
         .map(this::toDto)
         .toList();
    }

    private ComprobanteFacturacionResponse generarComprobantePendienteLocal(Pedido pedido, String tipoDoc, LocalDateTime fechaEmision) {
        LocalDateTime fechaUsar = fechaEmision != null ? fechaEmision : LocalDateTime.now();
        
        FacturacionSerie serieLocal = serieLocalRepository.findBySucursalIdAndTipoComprobanteAndActivoTrue(pedido.getSucursal().getId(), tipoDoc)
                .orElseThrow(() -> new RuntimeException("No se encontró una serie activa para la sucursal y tipo de comprobante especificados."));
        String serie = serieLocal.getSerie();

        Integer maxCorr = facturacionRepository.obtenerMaxCorrelativo(pedido.getSucursal().getId(), tipoDoc, serie);
        int maxExistente = maxCorr != null ? maxCorr : 0;
        int proximoConfigurado = serieLocal.getProximoCorrelativo() != null ? serieLocal.getProximoCorrelativo() : 1;
        int siguiente = Math.max(maxExistente + 1, proximoConfigurado);

        serieLocal.setProximoCorrelativo(siguiente + 1);
        serieLocalRepository.save(serieLocal);

        BigDecimal total = pedido.getTotalFinal();

        return new ComprobanteFacturacionResponse(
                null, // id
                tipoDoc,
                serie,
                siguiente,
                serie + "-" + String.format("%08d", siguiente),
                fechaUsar.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "PEN",
                BigDecimal.ZERO, // mtoOperGravadas
                total,           // mtoOperExoneradas
                BigDecimal.ZERO, // mtoOperInafectas
                BigDecimal.ZERO, // mtoIgv
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

        return new ComprobanteFacturacionResponse(
                null, // id
                "02",
                serie,
                siguiente,
                serie + "-" + String.format("%08d", siguiente),
                fechaUsar.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "PEN",
                BigDecimal.ZERO, // mtoOperGravadas
                total,           // mtoOperExoneradas
                BigDecimal.ZERO, // mtoOperInafectas
                BigDecimal.ZERO, // mtoIgv
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
                .map(this::toDto)
                .toList();
    }

    private FacturacionComprobante mapToEntity(ComprobanteFacturacionResponse api, Pedido pedido, Boolean impresionConsumo) {
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
                .facturadorId(api.id())
                .impresionConsumo(impresionConsumo)
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
                comprobante.setCdrSunatXml(cdrUrl);
            }

            // 4. Generamos y guardamos el TXT local
            String txtContent = generarTxtNotaDeVenta(comprobante, apiResponse);
            java.nio.file.Files.writeString(dir.resolve(baseName + ".txt"), txtContent);
            comprobante.setArchivoTxt(txtContent);

        } catch (Exception e) {
            log.error("Error al almacenar archivos locales del comprobante: {}", e.getMessage());
        }
    }

    private void descargarYGuardarArchivo(String urlString, java.nio.file.Path destinoPath) {
        if (urlString == null || urlString.isEmpty()) return;
        try {
            String token = comprobanteApiService.getAuthServiceValidToken();
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            try (java.io.InputStream in = conn.getInputStream()) {
                java.nio.file.Files.createDirectories(destinoPath.getParent());
                java.nio.file.Files.copy(in, destinoPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("Fallo al descargar y guardar archivo en {}: {}", destinoPath, e.getMessage());
        }
    }

    private String buildDownloadUrl(ComprobanteFacturacionResponse api, String tipo) {
        if (api == null || api.id() == null)
            return null;
        String formato = tipo.equalsIgnoreCase("pdf") ? "?formato=ticket" : "";
        return apiBaseUrl + "/api/v1/comprobantes/" + api.id() + "/download/" + tipo.toLowerCase() + formato;
    }

    private String generarTxtNotaDeVenta(FacturacionComprobante comprobante, ComprobanteFacturacionResponse apiResponse) {
        StringBuilder txt = new StringBuilder();
        Pedido pedido = comprobante.getPedido();
        
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
        if (Boolean.TRUE.equals(comprobante.getImpresionConsumo())) {
            totalVenta = comprobante.getTotalVenta();
            BigDecimal subtotal = totalVenta;
            txt.append(String.format("%-4s %-30s %12s %12s\n",
                    "1", "CONSUMO DE ALIMENTO", subtotal.setScale(2, RoundingMode.HALF_UP), totalVenta.setScale(2, RoundingMode.HALF_UP)));
        } else {
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
        }
        
        txt.append("--------------------------------------------------------\n");
        txt.append(String.format("%37s %12s\n", "Sub Total:", totalVenta.setScale(2, RoundingMode.HALF_UP)));
        txt.append(String.format("%37s %12s\n", "IGV (0%):", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
        txt.append(String.format("%37s %12s\n", "TOTAL:", totalVenta.setScale(2, RoundingMode.HALF_UP)));
        
        txt.append("\n");
        txt.append("Leyenda: Comprobante electrónico generado en sistema interno.\n");
        
        return txt.toString();
    }

    private FacturacionComprobanteDto toDto(FacturacionComprobante c) {
        Long minutos = null;
        if ("PENDIENTE_ENVIO".equals(c.getEstadoSunat()) && c.getFechaEmision() != null) {
            long diff = java.time.temporal.ChronoUnit.MINUTES.between(c.getFechaEmision(), LocalDateTime.now());
            long rem = 30 - diff;
            minutos = rem < 0 ? 0L : rem;
        }
        return new FacturacionComprobanteDto(
                c.getId(), c.getTipoComprobante(), c.getSerie(), c.getCorrelativo(),
                c.getRucEmisor(), c.getFechaEmision() != null ? c.getFechaEmision().toString() : null,
                c.getTotalVenta(),
                c.getPedido() != null ? c.getPedido().getId() : null,
                c.getArchivoXml(), c.getArchivoPdf(), c.getArchivoTxt(),
                c.getEstadoSunat(), c.getCdrSunatXml(), c.getSunatMensajeError(), minutos,
                c.getImpresionConsumo());
    }

    @Transactional(readOnly = true)
    public byte[] obtenerArchivoComprobante(String id, String tipo) {
        return obtenerArchivoComprobante(id, tipo, "ticket");
    }

    @Transactional(readOnly = true)
    public byte[] obtenerArchivoComprobante(String id, String tipo, String formato) {
        FacturacionComprobante comprobante = facturacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));

        String urlString = tipo.equalsIgnoreCase("pdf") ? comprobante.getArchivoPdf() : comprobante.getArchivoXml();
        if (urlString == null || urlString.isEmpty()) {
            throw new RuntimeException("El archivo solicitado no está disponible para este comprobante");
        }

        // Si es PDF y el formato solicitado es distinto, ajustamos el query parameter del urlString
        if (tipo.equalsIgnoreCase("pdf") && formato != null) {
            if (urlString.contains("?formato=")) {
                urlString = urlString.substring(0, urlString.indexOf("?formato=")) + "?formato=" + formato.toLowerCase();
            } else {
                urlString = urlString + "?formato=" + formato.toLowerCase();
            }
        }

        // Primero intentamos buscarlo localmente en "var/comprobantes"
        java.nio.file.Path rootPath = java.nio.file.Paths.get("var", "comprobantes");
        String anio = String.valueOf(comprobante.getFechaEmision().getYear());
        String mes = String.format("%02d", comprobante.getFechaEmision().getMonthValue());
        String tipoDoc = comprobante.getTipoComprobante();
        
        String suffix = tipo.equalsIgnoreCase("pdf")
                ? ("_" + formato.toLowerCase() + ".pdf")
                : ".xml";

        java.nio.file.Path file = rootPath.resolve(anio).resolve(mes).resolve(tipoDoc)
                .resolve(comprobante.getSerie() + "-" + comprobante.getCorrelativo() + suffix);

        if (java.nio.file.Files.exists(file)) {
            try {
                return java.nio.file.Files.readAllBytes(file);
            } catch (Exception e) {
                log.error("Error al leer archivo local: {}", e.getMessage());
            }
        }

        // Si no existe localmente, lo descargamos al vuelo usando el token de facturacion
        try {
            String token = comprobanteApiService.getAuthServiceValidToken();
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            try (java.io.InputStream in = conn.getInputStream()) {
                byte[] data = in.readAllBytes();
                // Guardarlo localmente para futuros accesos
                try {
                    java.nio.file.Files.createDirectories(file.getParent());
                    java.nio.file.Files.write(file, data);
                } catch (Exception e) {
                    log.error("Error al guardar archivo en cache local: {}", e.getMessage());
                }
                return data;
            }
        } catch (Exception e) {
            log.error("Error al descargar archivo del facturador al vuelo: {}", e.getMessage());
            throw new RuntimeException("Error al descargar el archivo del facturador: " + e.getMessage());
        }
    }
    @Transactional
    public void configurarSerieCorrelativo(String sucursalId, String tipoDoc, String serie, Integer correlativo) {
        var sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + sucursalId));

        String apiSucursalId = sucursal.getApiSucursalId();

        // Normalizar tipo doc a código SUNAT
        String tipoDocCodigo = switch (tipoDoc.toUpperCase()) {
            case "FACTURA", "01" -> "01";
            case "BOLETA", "03" -> "03";
            case "NOTA_CREDITO", "07" -> "07";
            case "NOTA_DEBITO", "08" -> "08";
            default -> tipoDoc;
        };

        // VALIDACIÓN LOCAL: misma empresa, distinta sucursal, mismo tipoDoc+serie
        boolean duplicadoLocal = serieLocalRepository
                .existsByEmpresaIdAndTipoComprobanteAndSerieAndSucursalIdNot(
                        sucursal.getEmpresa().getId(), tipoDocCodigo, serie.toUpperCase(), sucursalId);
        if (duplicadoLocal) {
            throw new RuntimeException("La serie " + serie.toUpperCase() +
                    " ya está registrada en otra sucursal de esta empresa.");
        }

        // GUARDAR O ACTUALIZAR en BD local
        var serieExistente = serieLocalRepository
                .findBySucursalIdAndTipoComprobanteAndSerie(sucursalId, tipoDocCodigo, serie.toUpperCase());

        FacturacionSerie serieLocal;
        if (serieExistente.isPresent()) {
            serieLocal = serieExistente.get();
            serieLocal.setProximoCorrelativo(correlativo);
            serieLocal.setActivo(true);
        } else {
            serieLocal = FacturacionSerie.builder()
                    .tipoComprobante(tipoDocCodigo)
                    .serie(serie.toUpperCase())
                    .proximoCorrelativo(correlativo)
                    .sucursal(sucursal)
                    .empresa(sucursal.getEmpresa())
                    .activo(true)
                    .build();
        }
        serieLocalRepository.save(serieLocal);
        log.info("Serie guardada localmente: {} {} correlativo={}", tipoDocCodigo, serie.toUpperCase(), correlativo);

        // ENVIAR A API EXTERNA (no-bloqueante ante error)
        if (apiSucursalId != null) {
            try {
                String token = comprobanteApiService.getAuthServiceValidToken();
                String companyId = sucursal.getEmpresa().getApiCompanyId();
                if (companyId == null) {
                    comprobanteApiService.ensureCompanyExists(sucursal.getEmpresa());
                    companyId = sucursal.getEmpresa().getApiCompanyId();
                }
                if (companyId != null) {
                    comprobanteApiService.configurarInicioSerieEnApi(
                            companyId, apiSucursalId, tipoDoc, serie, correlativo, token);
                    log.info("Serie enviada a API externa exitosamente: {}", serie.toUpperCase());
                }
            } catch (Exception e) {
                log.warn("No se pudo enviar la serie a la API externa (ya está guardada localmente): {}", e.getMessage());
            }
        }
    }

    @Transactional
    public List<java.util.Map<String, Object>> obtenerSeriesPorSucursal(String sucursalId) {
        var sucursalOpt = sucursalRepository.findById(sucursalId);
        if (sucursalOpt.isPresent()) {
            var sucursal = sucursalOpt.get();
            String apiSucursalId = sucursal.getApiSucursalId();
            String companyId = sucursal.getEmpresa().getApiCompanyId();
            if (apiSucursalId != null && companyId != null) {
                try {
                    String token = comprobanteApiService.getAuthServiceValidToken();
                    List<java.util.Map<String, Object>> seriesApi = comprobanteApiService.listarSeriesEnApi(companyId, apiSucursalId, token);
                    if (seriesApi != null && !seriesApi.isEmpty()) {
                        for (java.util.Map<String, Object> sApi : seriesApi) {
                            String codSerie = (String) sApi.get("serie");
                            String codTipoDoc = (String) sApi.get("tipoDocCodigo");
                            Integer ultimoCorr = (Integer) sApi.get("ultimoCorrelativo");
                            if (codSerie != null && codTipoDoc != null && ultimoCorr != null) {
                                var serieExistente = serieLocalRepository.findBySucursalIdAndTipoComprobanteAndSerie(
                                        sucursalId, codTipoDoc, codSerie.toUpperCase());
                                if (serieExistente.isPresent()) {
                                    var sLocal = serieExistente.get();
                                    sLocal.setProximoCorrelativo(ultimoCorr + 1);
                                    serieLocalRepository.save(sLocal);
                                }
                            }
                        }
                        serieLocalRepository.flush();
                        log.info("Correlativos sincronizados exitosamente con API de Facturación para sucursal {}", sucursalId);
                    }
                } catch (Exception e) {
                    log.warn("API de facturación externa no disponible (offline). Usando correlativos locales. Detalle: {}", e.getMessage());
                }
            }
        }

        // Leer siempre desde la BD local (fuente de verdad sincronizada o fallback local)
        List<FacturacionSerie> seriesLocales = serieLocalRepository.findBySucursalIdAndActivoTrue(sucursalId);
        return seriesLocales.stream()
                .map(s -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("serie", s.getSerie());
                    m.put("tipoDocCodigo", s.getTipoComprobante());
                    m.put("tipoDoc", switch (s.getTipoComprobante()) {
                        case "01" -> "FACTURA";
                        case "03" -> "BOLETA";
                        case "07" -> "NOTA_CREDITO";
                        case "08" -> "NOTA_DEBITO";
                        default -> s.getTipoComprobante();
                    });
                    m.put("ultimoCorrelativo", s.getProximoCorrelativo() - 1);
                    m.put("proximoNumero", s.getSerie() + "-" + String.format("%08d", s.getProximoCorrelativo()));
                    m.put("activo", s.getActivo());
                    m.put("sucursalId", s.getSucursal() != null ? s.getSucursal().getApiSucursalId() : null);
                    m.put("sucursalNombre", s.getSucursal() != null ? s.getSucursal().getNombre() : null);
                    return m;
                })
                .toList();
    }

    private void registrarMovimientoCajaSiAceptado(FacturacionComprobante comprobante) {
        if (!"ACEPTADO".equals(comprobante.getEstadoSunat())) {
            return;
        }
        Pedido pedido = comprobante.getPedido();
        if (pedido == null || pedido.getCajaTurno() == null) {
            return;
        }

        // Evitar duplicar movimientos para el mismo pedido en la caja
        String prefixDesc = "Venta " + pedido.getCodigoPedido();
        boolean yaExiste = movimientoRepository.existsByCajaTurnoIdAndDescripcionContaining(
                pedido.getCajaTurno().getId(), prefixDesc);
        
        if (yaExiste) {
            return;
        }

        // Obtener los pagos registrados del pedido
        List<com.restaurante.resturante.domain.ventas.PedidoPago> pagos = pedido.getPagos();
        if (pagos == null || pagos.isEmpty()) {
            return;
        }

        for (com.restaurante.resturante.domain.ventas.PedidoPago pago : pagos) {
            String tipoEnt = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "MESA";
            String infoMesa = (pedido.getMesa() != null) ? " - " + pedido.getMesa().getCodigoMesa() : "";
            
            MovimientoCaja mov = MovimientoCaja.builder()
                    .cajaTurno(pedido.getCajaTurno())
                    .usuario(pedido.getCajaTurno().getUser() != null ? pedido.getCajaTurno().getUser() : pedido.getUser())
                    .tipo(TipoMovimiento.INGRESO)
                    .monto(pago.getMonto())
                    .descripcion("Venta " + pedido.getCodigoPedido() + " (" + tipoEnt + infoMesa + ") - Medio: " + pago.getMedioPago().getNombre())
                    .fecha(LocalDateTime.now())
                    .esEfectivo(pago.getMedioPago().isEsEfectivo())
                    .build();
            movimientoRepository.save(mov);
        }
    }
}

