package com.restaurante.resturante.service.venta.jpa;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurante.resturante.domain.inventario.Inventario;
import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.PedidoPago;
import com.restaurante.resturante.domain.ventas.TipoMovimiento;
import com.restaurante.resturante.dto.venta.ReportesDto.CajaAuditDto;
import com.restaurante.resturante.dto.venta.ReportesDto.MenuRankingDto;
import com.restaurante.resturante.dto.venta.ReportesDto.ProductoBajoStockDto;
import com.restaurante.resturante.dto.venta.ReportesDto.SalesSummaryDto;
import com.restaurante.resturante.dto.venta.ReportesDto.StockCriticalDto;
import com.restaurante.resturante.dto.venta.ReportesDto.VentaDiariaDto;
import com.restaurante.resturante.dto.venta.ReportesDto.WaiterPerformanceDto;
import com.restaurante.resturante.service.venta.IReporteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReporteService implements IReporteService {

    @PersistenceContext
    private EntityManager entityManager;

    private void validarSucursales(List<String> sucursalIds) {
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            for (String id : sucursalIds) {
                if (id == null || id.trim().isEmpty()) {
                    continue;
                }
                Long count = entityManager.createQuery(
                        "SELECT COUNT(s) FROM Sucursal s WHERE s.id = :id AND s.estado = true", Long.class)
                        .setParameter("id", id)
                        .getSingleResult();
                if (count == 0) {
                    throw new IllegalArgumentException("Sucursal no encontrada o inactiva: " + id);
                }
            }
        }
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    @Override
    public SalesSummaryDto getSalesSummary(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        validarSucursales(sucursalIds);
        validarFechas(fechaInicio, fechaFin);

        LocalDateTime startDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime endDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59, 999999) : LocalDate.now().atTime(23, 59, 59, 999999);

        // 1. Total ventas y cantidad de pedidos
        String sumJpql = "SELECT COALESCE(SUM(p.totalFinal), 0), COUNT(p) FROM Pedido p WHERE p.estado = 'PAGADO'";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            sumJpql += " AND p.sucursal.id IN :sucursalIds";
        }
        sumJpql += " AND p.fechaCreacion >= :start AND p.fechaCreacion <= :end";

        TypedQuery<Object[]> sumQuery = entityManager.createQuery(sumJpql, Object[].class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            sumQuery.setParameter("sucursalIds", sucursalIds);
        }

        Object[] summaryRow = sumQuery.getSingleResult();
        BigDecimal totalVentas = (BigDecimal) summaryRow[0];
        long cantidadPedidos = ((Number) summaryRow[1]).longValue();
        BigDecimal promedioTicket = cantidadPedidos > 0 
                ? totalVentas.divide(BigDecimal.valueOf(cantidadPedidos), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        // 2. Desglose de pagos
        String pagJpql = "SELECT pp FROM PedidoPago pp JOIN FETCH pp.medioPago JOIN pp.pedido p WHERE p.estado = 'PAGADO'";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            pagJpql += " AND p.sucursal.id IN :sucursalIds";
        }
        pagJpql += " AND p.fechaCreacion >= :start AND p.fechaCreacion <= :end";

        TypedQuery<PedidoPago> pagQuery = entityManager.createQuery(pagJpql, PedidoPago.class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            pagQuery.setParameter("sucursalIds", sucursalIds);
        }

        List<PedidoPago> pagos = pagQuery.getResultList();

        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjeta = BigDecimal.ZERO;
        BigDecimal totalYape = BigDecimal.ZERO;
        BigDecimal totalPlin = BigDecimal.ZERO;
        BigDecimal totalOtros = BigDecimal.ZERO;

        for (PedidoPago pago : pagos) {
            BigDecimal m = pago.getMonto() != null ? pago.getMonto() : BigDecimal.ZERO;
            String name = pago.getMedioPago().getNombre().toUpperCase();
            if (name.contains("EFECTIVO")) {
                totalEfectivo = totalEfectivo.add(m);
            } else if (name.contains("TARJETA")) {
                totalTarjeta = totalTarjeta.add(m);
            } else if (name.contains("YAPE")) {
                totalYape = totalYape.add(m);
            } else if (name.contains("PLIN")) {
                totalPlin = totalPlin.add(m);
            } else {
                totalOtros = totalOtros.add(m);
            }
        }

        // 3. Ventas diarias
        String orderJpql = "SELECT p.fechaCreacion, p.totalFinal FROM Pedido p WHERE p.estado = 'PAGADO'";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            orderJpql += " AND p.sucursal.id IN :sucursalIds";
        }
        orderJpql += " AND p.fechaCreacion >= :start AND p.fechaCreacion <= :end";

        TypedQuery<Object[]> orderQuery = entityManager.createQuery(orderJpql, Object[].class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            orderQuery.setParameter("sucursalIds", sucursalIds);
        }

        List<Object[]> orders = orderQuery.getResultList();

        Map<LocalDate, List<BigDecimal>> dailyTotals = new TreeMap<>();
        for (Object[] row : orders) {
            LocalDateTime ldt = (LocalDateTime) row[0];
            BigDecimal total = (BigDecimal) row[1];
            LocalDate date = ldt.toLocalDate();
            dailyTotals.computeIfAbsent(date, k -> new ArrayList<>()).add(total);
        }

        List<VentaDiariaDto> ventasDiarias = new ArrayList<>();
        for (Map.Entry<LocalDate, List<BigDecimal>> entry : dailyTotals.entrySet()) {
            BigDecimal daySum = entry.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            long dayCount = entry.getValue().size();
            ventasDiarias.add(new VentaDiariaDto(entry.getKey(), daySum, dayCount));
        }

        return new SalesSummaryDto(
                totalVentas,
                promedioTicket,
                totalEfectivo,
                totalTarjeta,
                totalYape,
                totalPlin,
                totalOtros,
                ventasDiarias
        );
    }

    @Override
    public List<MenuRankingDto> getMenuRanking(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        validarSucursales(sucursalIds);
        validarFechas(fechaInicio, fechaFin);

        LocalDateTime startDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime endDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59, 999999) : LocalDate.now().atTime(23, 59, 59, 999999);

        String jpql = "SELECT pd.producto.nombreProducto, SUM(pd.cantidad), SUM(pd.totalLinea) " +
                "FROM PedidoDetalle pd WHERE pd.pedido.estado = 'PAGADO'";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            jpql += " AND pd.pedido.sucursal.id IN :sucursalIds";
        }
        jpql += " AND pd.pedido.fechaCreacion >= :start AND pd.pedido.fechaCreacion <= :end";
        jpql += " GROUP BY pd.producto.nombreProducto ORDER BY SUM(pd.cantidad) DESC";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime)
                .setMaxResults(10);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            query.setParameter("sucursalIds", sucursalIds);
        }

        List<Object[]> rows = query.getResultList();
        List<MenuRankingDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            String nombrePlato = (String) row[0];
            long cantidad = ((Number) row[1]).longValue();
            BigDecimal ingresos = (BigDecimal) row[2];
            list.add(new MenuRankingDto(nombrePlato, cantidad, ingresos));
        }
        return list;
    }

    @Override
    public List<CajaAuditDto> getCajaAudit(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        validarSucursales(sucursalIds);
        validarFechas(fechaInicio, fechaFin);

        LocalDateTime startDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime endDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59, 999999) : LocalDate.now().atTime(23, 59, 59, 999999);

        String jpql = "SELECT ct FROM CajaTurno ct JOIN FETCH ct.sucursal JOIN FETCH ct.user WHERE 1=1";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            jpql += " AND ct.sucursal.id IN :sucursalIds";
        }
        jpql += " AND ct.fechaApertura >= :start AND ct.fechaApertura <= :end";
        jpql += " ORDER BY ct.fechaApertura DESC";

        TypedQuery<CajaTurno> query = entityManager.createQuery(jpql, CajaTurno.class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            query.setParameter("sucursalIds", sucursalIds);
        }

        List<CajaTurno> cajaTurnos = query.getResultList();
        List<String> cajaIds = cajaTurnos.stream().map(CajaTurno::getId).toList();

        Map<String, BigDecimal> ingresosMap = new HashMap<>();
        Map<String, BigDecimal> egresosMap = new HashMap<>();

        if (!cajaIds.isEmpty()) {
            String movJpql = "SELECT mc.cajaTurno.id, mc.tipo, SUM(mc.monto) FROM MovimientoCaja mc " +
                    "WHERE mc.cajaTurno.id IN :cajaIds GROUP BY mc.cajaTurno.id, mc.tipo";
            List<Object[]> movs = entityManager.createQuery(movJpql, Object[].class)
                    .setParameter("cajaIds", cajaIds)
                    .getResultList();

            for (Object[] row : movs) {
                String cajaId = (String) row[0];
                TipoMovimiento tipo = (TipoMovimiento) row[1];
                BigDecimal sum = (BigDecimal) row[2];
                if (tipo == TipoMovimiento.INGRESO) {
                    ingresosMap.put(cajaId, sum);
                } else if (tipo == TipoMovimiento.EGRESO) {
                    egresosMap.put(cajaId, sum);
                }
            }
        }

        List<CajaAuditDto> list = new ArrayList<>();
        for (CajaTurno ct : cajaTurnos) {
            String usuarioNombre = ct.getUser().getNombres() != null 
                    ? ct.getUser().getNombres() + " " + (ct.getUser().getApellidoPaterno() != null ? ct.getUser().getApellidoPaterno() : "")
                    : ct.getUser().getUsername();
            BigDecimal ing = ingresosMap.getOrDefault(ct.getId(), BigDecimal.ZERO);
            BigDecimal egr = egresosMap.getOrDefault(ct.getId(), BigDecimal.ZERO);

            list.add(new CajaAuditDto(
                    ct.getId(),
                    ct.getSucursal().getNombre(),
                    usuarioNombre,
                    ct.getFechaApertura(),
                    ct.getFechaCierre(),
                    ct.getMontoApertura() != null ? ct.getMontoApertura() : BigDecimal.ZERO,
                    ing,
                    egr,
                    ct.getMontoCierreEsperado() != null ? ct.getMontoCierreEsperado() : BigDecimal.ZERO,
                    ct.getMontoCierreReal() != null ? ct.getMontoCierreReal() : BigDecimal.ZERO,
                    ct.getDiferencia() != null ? ct.getDiferencia() : BigDecimal.ZERO,
                    ct.getEstado()
            ));
        }

        return list;
    }

    @Override
    public StockCriticalDto getStockCritical(List<String> sucursalIds) {
        validarSucursales(sucursalIds);

        String jpql = "SELECT i FROM Inventario i JOIN FETCH i.producto JOIN FETCH i.sucursal WHERE 1=1";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            jpql += " AND i.sucursal.id IN :sucursalIds";
        }

        TypedQuery<Inventario> query = entityManager.createQuery(jpql, Inventario.class);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            query.setParameter("sucursalIds", sucursalIds);
        }

        List<Inventario> list = query.getResultList();

        BigDecimal totalValuation = BigDecimal.ZERO;
        List<ProductoBajoStockDto> bajoStock = new ArrayList<>();

        for (Inventario i : list) {
            BigDecimal stockAct = i.getStockActual() != null ? BigDecimal.valueOf(i.getStockActual()) : BigDecimal.ZERO;
            BigDecimal stockMin = i.getStockMinimo() != null ? BigDecimal.valueOf(i.getStockMinimo()) : BigDecimal.ZERO;
            BigDecimal costo = i.getProducto().getCostoCompra() != null ? i.getProducto().getCostoCompra() : BigDecimal.ZERO;

            totalValuation = totalValuation.add(stockAct.multiply(costo));

            if (i.getStockActual() != null && i.getStockMinimo() != null && i.getStockActual() < i.getStockMinimo()) {
                bajoStock.add(new ProductoBajoStockDto(
                        i.getProducto().getIdProducto(),
                        i.getProducto().getNombreProducto(),
                        i.getSucursal().getNombre(),
                        stockAct,
                        stockMin,
                        i.getProducto().getUnidadMedida() != null ? i.getProducto().getUnidadMedida() : "UNIDAD"
                ));
            }
        }

        return new StockCriticalDto(totalValuation, bajoStock);
    }

    @Override
    public List<WaiterPerformanceDto> getWaiterPerformance(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        validarSucursales(sucursalIds);
        validarFechas(fechaInicio, fechaFin);

        LocalDateTime startDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime endDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59, 999999) : LocalDate.now().atTime(23, 59, 59, 999999);

        String jpql = "SELECT p.user, COUNT(p), SUM(p.totalFinal) FROM Pedido p WHERE p.estado = 'PAGADO'";
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            jpql += " AND p.sucursal.id IN :sucursalIds";
        }
        jpql += " AND p.fechaCreacion >= :start AND p.fechaCreacion <= :end";
        jpql += " GROUP BY p.user ORDER BY COUNT(p) DESC";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class)
                .setParameter("start", startDateTime)
                .setParameter("end", endDateTime);
        if (sucursalIds != null && !sucursalIds.isEmpty()) {
            query.setParameter("sucursalIds", sucursalIds);
        }

        List<Object[]> rows = query.getResultList();
        List<WaiterPerformanceDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            com.restaurante.resturante.domain.security.User u = (com.restaurante.resturante.domain.security.User) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal total = (BigDecimal) row[2];

            String waiterNombre = u.getNombres() != null 
                    ? u.getNombres() + " " + (u.getApellidoPaterno() != null ? u.getApellidoPaterno() : "")
                    : u.getUsername();

            list.add(new WaiterPerformanceDto(waiterNombre, count, total));
        }

        return list;
    }

    @Override
    public byte[] exportToPdf(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Título principal
            String displayTitle = "Reporte de " + reportType.toUpperCase();
            document.add(new Paragraph(displayTitle, titleFont));
            document.add(new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), bodyFont));
            if (fechaInicio != null && fechaFin != null) {
                document.add(new Paragraph("Rango: " + fechaInicio.toString() + " al " + fechaFin.toString(), bodyFont));
            }
            document.add(new Paragraph("\n"));

            if ("sales".equalsIgnoreCase(reportType)) {
                SalesSummaryDto dto = getSalesSummary(sucursalIds, fechaInicio, fechaFin);
                document.add(new Paragraph("Resumen de Ventas", sectionFont));
                document.add(new Paragraph("\n"));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                
                table.addCell(new PdfPCell(new Phrase("Concepto", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Monto (S/.)", headerFont)));

                table.addCell("Total Ventas");
                table.addCell(dto.totalVentas().toString());

                table.addCell("Ticket Promedio");
                table.addCell(dto.promedioTicket().toString());

                table.addCell("Total Efectivo");
                table.addCell(dto.totalEfectivo().toString());

                table.addCell("Total Tarjeta");
                table.addCell(dto.totalTarjeta().toString());

                table.addCell("Total Yape");
                table.addCell(dto.totalYape().toString());

                table.addCell("Total Plin");
                table.addCell(dto.totalPlin().toString());

                table.addCell("Total Otros");
                table.addCell(dto.totalOtros().toString());

                document.add(table);

                document.add(new Paragraph("\nDetalle de Ventas Diarias", sectionFont));
                document.add(new Paragraph("\n"));

                PdfPTable dTable = new PdfPTable(3);
                dTable.setWidthPercentage(100);
                dTable.addCell(new PdfPCell(new Phrase("Fecha", headerFont)));
                dTable.addCell(new PdfPCell(new Phrase("Cantidad Pedidos", headerFont)));
                dTable.addCell(new PdfPCell(new Phrase("Total Diario (S/.)", headerFont)));

                for (VentaDiariaDto v : dto.ventasDiarias()) {
                    dTable.addCell(v.fecha().toString());
                    dTable.addCell(String.valueOf(v.cantidadPedidos()));
                    dTable.addCell(v.total().toString());
                }
                document.add(dTable);

            } else if ("menu-ranking".equalsIgnoreCase(reportType)) {
                List<MenuRankingDto> list = getMenuRanking(sucursalIds, fechaInicio, fechaFin);
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Puesto", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Plato", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Cantidad", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Ingresos (S/.)", headerFont)));

                int idx = 1;
                for (MenuRankingDto r : list) {
                    table.addCell(String.valueOf(idx++));
                    table.addCell(r.nombrePlato());
                    table.addCell(String.valueOf(r.cantidad()));
                    table.addCell(r.ingresos().toString());
                }
                document.add(table);

            } else if ("caja-audit".equalsIgnoreCase(reportType)) {
                List<CajaAuditDto> list = getCajaAudit(sucursalIds, fechaInicio, fechaFin);
                PdfPTable table = new PdfPTable(8);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Sucursal", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Usuario", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Apertura", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Inicial", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Ingresos", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Egresos", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Esperado", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Real", headerFont)));

                for (CajaAuditDto c : list) {
                    table.addCell(c.sucursalNombre());
                    table.addCell(c.usuarioNombre());
                    table.addCell(c.fechaApertura().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                    table.addCell(c.montoInicial().toString());
                    table.addCell(c.ingresosCajaChica().toString());
                    table.addCell(c.egresosCajaChica().toString());
                    table.addCell(c.saldoEsperado().toString());
                    table.addCell(c.saldoReal().toString());
                }
                document.add(table);

            } else if ("stock-critical".equalsIgnoreCase(reportType)) {
                StockCriticalDto dto = getStockCritical(sucursalIds);
                document.add(new Paragraph("Valoración Total Inventario: S/. " + dto.valoracionTotalInventario().toString(), sectionFont));
                document.add(new Paragraph("\n"));

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Producto", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Sucursal", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Stock Act", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Stock Min", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Medida", headerFont)));

                for (ProductoBajoStockDto p : dto.productosBajoStock()) {
                    table.addCell(p.productoId().toString());
                    table.addCell(p.nombreProducto());
                    table.addCell(p.sucursalNombre());
                    table.addCell(p.stockActual().toString());
                    table.addCell(p.stockMinimo().toString());
                    table.addCell(p.unidadMedida());
                }
                document.add(table);

            } else if ("waiter-performance".equalsIgnoreCase(reportType)) {
                List<WaiterPerformanceDto> list = getWaiterPerformance(sucursalIds, fechaInicio, fechaFin);
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Mozo / Waiter", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Cantidad Pedidos", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Total Vendido (S/.)", headerFont)));

                for (WaiterPerformanceDto w : list) {
                    table.addCell(w.waiterNombre());
                    table.addCell(String.valueOf(w.cantidadPedidos()));
                    table.addCell(w.totalVendido().toString());
                }
                document.add(table);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportToExcel(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            if ("sales".equalsIgnoreCase(reportType)) {
                SalesSummaryDto dto = getSalesSummary(sucursalIds, fechaInicio, fechaFin);
                
                // Resumen
                Row r0 = sheet.createRow(0);
                r0.createCell(0).setCellValue("Concepto");
                r0.createCell(1).setCellValue("Valor (S/.)");
                r0.getCell(0).setCellStyle(headerStyle);
                r0.getCell(1).setCellStyle(headerStyle);

                String[] concepts = {"Total Ventas", "Ticket Promedio", "Total Efectivo", "Total Tarjeta", "Total Yape", "Total Plin", "Total Otros"};
                BigDecimal[] values = {dto.totalVentas(), dto.promedioTicket(), dto.totalEfectivo(), dto.totalTarjeta(), dto.totalYape(), dto.totalPlin(), dto.totalOtros()};

                for (int i = 0; i < concepts.length; i++) {
                    Row r = sheet.createRow(i + 1);
                    r.createCell(0).setCellValue(concepts[i]);
                    r.createCell(1).setCellValue(values[i].doubleValue());
                }

                // Ventas diarias
                Sheet dSheet = workbook.createSheet("Ventas Diarias");
                Row dr0 = dSheet.createRow(0);
                dr0.createCell(0).setCellValue("Fecha");
                dr0.createCell(1).setCellValue("Pedidos");
                dr0.createCell(2).setCellValue("Total (S/.)");
                dr0.getCell(0).setCellStyle(headerStyle);
                dr0.getCell(1).setCellStyle(headerStyle);
                dr0.getCell(2).setCellStyle(headerStyle);

                int idx = 1;
                for (VentaDiariaDto v : dto.ventasDiarias()) {
                    Row r = dSheet.createRow(idx++);
                    r.createCell(0).setCellValue(v.fecha().toString());
                    r.createCell(1).setCellValue(v.cantidadPedidos());
                    r.createCell(2).setCellValue(v.total().doubleValue());
                }

            } else if ("menu-ranking".equalsIgnoreCase(reportType)) {
                List<MenuRankingDto> list = getMenuRanking(sucursalIds, fechaInicio, fechaFin);
                Row r0 = sheet.createRow(0);
                r0.createCell(0).setCellValue("Puesto");
                r0.createCell(1).setCellValue("Plato");
                r0.createCell(2).setCellValue("Cantidad");
                r0.createCell(3).setCellValue("Ingresos (S/.)");
                for (int i = 0; i < 4; i++) r0.getCell(i).setCellStyle(headerStyle);

                int idx = 1;
                for (MenuRankingDto r : list) {
                    Row row = sheet.createRow(idx);
                    row.createCell(0).setCellValue(idx);
                    row.createCell(1).setCellValue(r.nombrePlato());
                    row.createCell(2).setCellValue(r.cantidad());
                    row.createCell(3).setCellValue(r.ingresos().doubleValue());
                    idx++;
                }

            } else if ("caja-audit".equalsIgnoreCase(reportType)) {
                List<CajaAuditDto> list = getCajaAudit(sucursalIds, fechaInicio, fechaFin);
                Row r0 = sheet.createRow(0);
                String[] headers = {"Sucursal", "Usuario", "Apertura", "Cierre", "Monto Inicial", "Ingresos Chica", "Egresos Chica", "Esperado", "Real", "Diferencia", "Estado"};
                for (int i = 0; i < headers.length; i++) {
                    Cell c = r0.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                int idx = 1;
                for (CajaAuditDto c : list) {
                    Row row = sheet.createRow(idx++);
                    row.createCell(0).setCellValue(c.sucursalNombre());
                    row.createCell(1).setCellValue(c.usuarioNombre());
                    row.createCell(2).setCellValue(c.fechaApertura().toString());
                    row.createCell(3).setCellValue(c.fechaCierre() != null ? c.fechaCierre().toString() : "-");
                    row.createCell(4).setCellValue(c.montoInicial().doubleValue());
                    row.createCell(5).setCellValue(c.ingresosCajaChica().doubleValue());
                    row.createCell(6).setCellValue(c.egresosCajaChica().doubleValue());
                    row.createCell(7).setCellValue(c.saldoEsperado().doubleValue());
                    row.createCell(8).setCellValue(c.saldoReal().doubleValue());
                    row.createCell(9).setCellValue(c.diferencia().doubleValue());
                    row.createCell(10).setCellValue(c.estado());
                }

            } else if ("stock-critical".equalsIgnoreCase(reportType)) {
                StockCriticalDto dto = getStockCritical(sucursalIds);
                
                Row vRow = sheet.createRow(0);
                vRow.createCell(0).setCellValue("Valoración Total Inventario:");
                vRow.createCell(1).setCellValue(dto.valoracionTotalInventario().doubleValue());
                vRow.getCell(0).setCellStyle(headerStyle);

                Row r0 = sheet.createRow(2);
                String[] headers = {"ID Producto", "Producto", "Sucursal", "Stock Actual", "Stock Mínimo", "Medida"};
                for (int i = 0; i < headers.length; i++) {
                    Cell c = r0.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                int idx = 3;
                for (ProductoBajoStockDto p : dto.productosBajoStock()) {
                    Row row = sheet.createRow(idx++);
                    row.createCell(0).setCellValue(p.productoId());
                    row.createCell(1).setCellValue(p.nombreProducto());
                    row.createCell(2).setCellValue(p.sucursalNombre());
                    row.createCell(3).setCellValue(p.stockActual().doubleValue());
                    row.createCell(4).setCellValue(p.stockMinimo().doubleValue());
                    row.createCell(5).setCellValue(p.unidadMedida());
                }

            } else if ("waiter-performance".equalsIgnoreCase(reportType)) {
                List<WaiterPerformanceDto> list = getWaiterPerformance(sucursalIds, fechaInicio, fechaFin);
                Row r0 = sheet.createRow(0);
                r0.createCell(0).setCellValue("Mozo / Waiter");
                r0.createCell(1).setCellValue("Cantidad Pedidos");
                r0.createCell(2).setCellValue("Total Vendido (S/.)");
                for (int i = 0; i < 3; i++) r0.getCell(i).setCellStyle(headerStyle);

                int idx = 1;
                for (WaiterPerformanceDto w : list) {
                    Row row = sheet.createRow(idx++);
                    row.createCell(0).setCellValue(w.waiterNombre());
                    row.createCell(1).setCellValue(w.cantidadPedidos());
                    row.createCell(2).setCellValue(w.totalVendido().doubleValue());
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }
}
