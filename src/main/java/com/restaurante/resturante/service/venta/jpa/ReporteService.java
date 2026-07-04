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

    // ─── PDF ────────────────────────────────────────────────────────────────────

    @Override
    public byte[] exportToPdf(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── Color palette ────────────────────────────────────────────────
            java.awt.Color BRAND_DARK  = new java.awt.Color(24, 24, 27);
            java.awt.Color BRAND_LIGHT = new java.awt.Color(244, 244, 245);
            java.awt.Color ACCENT      = new java.awt.Color(99, 102, 241);
            java.awt.Color ROW_ALT     = new java.awt.Color(249, 250, 251);
            java.awt.Color BORDER_CLR  = new java.awt.Color(228, 228, 231);
            java.awt.Color DANGER_BG   = new java.awt.Color(254, 242, 242);
            java.awt.Color SUCCESS_BG  = new java.awt.Color(240, 253, 244);
            java.awt.Color WARNING_BG  = new java.awt.Color(255, 247, 237);

            // ── Fonts ────────────────────────────────────────────────────────
            Font fTitle    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.WHITE);
            Font fSubtitle = FontFactory.getFont(FontFactory.HELVETICA,       9, new java.awt.Color(161, 161, 170));
            Font fSection  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_DARK);
            Font fHeader   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  9, java.awt.Color.WHITE);
            Font fBody     = FontFactory.getFont(FontFactory.HELVETICA,       9, BRAND_DARK);
            Font fBodySm   = FontFactory.getFont(FontFactory.HELVETICA,       8, new java.awt.Color(113, 113, 122));
            Font fMono     = FontFactory.getFont(FontFactory.COURIER,         9, BRAND_DARK);
            Font fKpi      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, ACCENT);

            DateTimeFormatter dtFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter dtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String now = LocalDateTime.now().format(dtFmt);

            String displayTitle = switch (reportType.toLowerCase()) {
                case "sales"              -> "Ventas y Facturación";
                case "menu-ranking"       -> "Ranking del Menú";
                case "caja-audit"         -> "Arqueo y Cierre de Caja";
                case "stock-critical"     -> "Alertas de Stock Crítico";
                case "waiter-performance" -> "Desempeño del Personal";
                default                   -> "Reporte";
            };
            String rangoText = (fechaInicio != null && fechaFin != null)
                    ? fechaInicio.format(dtDate) + "  →  " + fechaFin.format(dtDate)
                    : "Todos los períodos";

            // ── Header band ──────────────────────────────────────────────────
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(BRAND_DARK);
            hCell.setPadding(18);
            hCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            Paragraph pTitle = new Paragraph(displayTitle, fTitle);
            pTitle.setSpacingAfter(5);
            Paragraph pMeta = new Paragraph("Período: " + rangoText + "   •   Generado: " + now, fSubtitle);
            hCell.addElement(pTitle);
            hCell.addElement(pMeta);
            headerTable.addCell(hCell);
            doc.add(headerTable);

            // ── Cell builders ────────────────────────────────────────────────
            java.util.function.Function<String, PdfPCell> hdrCell = col -> {
                PdfPCell c = new PdfPCell(new Phrase(col, fHeader));
                c.setBackgroundColor(ACCENT);
                c.setPadding(7);
                c.setBorderColor(BORDER_CLR);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                return c;
            };
            java.util.function.BiFunction<String, java.awt.Color, PdfPCell> bodyCell = (val, bg) -> {
                PdfPCell c = new PdfPCell(new Phrase(val, fBody));
                c.setPadding(6); c.setBorderColor(BORDER_CLR); c.setBackgroundColor(bg);
                return c;
            };
            java.util.function.BiFunction<String, java.awt.Color, PdfPCell> numCell = (val, bg) -> {
                PdfPCell c = new PdfPCell(new Phrase(val, fMono));
                c.setPadding(6); c.setBorderColor(BORDER_CLR);
                c.setBackgroundColor(bg);
                c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                return c;
            };
            java.util.function.Consumer<String> addSection = title -> {
                try {
                    Paragraph p = new Paragraph(title, fSection);
                    p.setSpacingBefore(14); p.setSpacingAfter(6);
                    doc.add(p);
                } catch (DocumentException ignored) {}
            };

            // ── Report content ───────────────────────────────────────────────
            if ("sales".equalsIgnoreCase(reportType)) {
                SalesSummaryDto dto = getSalesSummary(sucursalIds, fechaInicio, fechaFin);

                addSection.accept("Resumen General");
                PdfPTable kpi = new PdfPTable(new float[]{3, 2});
                kpi.setWidthPercentage(55);
                kpi.setSpacingAfter(16);
                for (String col : new String[]{"Concepto", "Monto (S/.)"}) kpi.addCell(hdrCell.apply(col));

                String[][] kpiRows = {
                    {"Total Ventas",    "S/. " + dto.totalVentas()},
                    {"Ticket Promedio", "S/. " + dto.promedioTicket()},
                    {"Efectivo",        "S/. " + dto.totalEfectivo()},
                    {"Tarjeta",         "S/. " + dto.totalTarjeta()},
                    {"Yape",            "S/. " + dto.totalYape()},
                    {"Plin",            "S/. " + dto.totalPlin()},
                    {"Otros",           "S/. " + dto.totalOtros()},
                };
                boolean alt = false;
                for (String[] row : kpiRows) {
                    java.awt.Color bg = alt ? ROW_ALT : java.awt.Color.WHITE;
                    kpi.addCell(bodyCell.apply(row[0], bg));
                    kpi.addCell(numCell.apply(row[1], bg));
                    alt = !alt;
                }
                doc.add(kpi);

                addSection.accept("Detalle de Ventas Diarias");
                PdfPTable dt = new PdfPTable(new float[]{2, 1.5f, 2});
                dt.setWidthPercentage(100);
                for (String col : new String[]{"Fecha", "Pedidos", "Total (S/.)"}) dt.addCell(hdrCell.apply(col));
                alt = false;
                for (VentaDiariaDto v : dto.ventasDiarias()) {
                    java.awt.Color bg = alt ? ROW_ALT : java.awt.Color.WHITE;
                    dt.addCell(bodyCell.apply(v.fecha().format(dtDate), bg));
                    dt.addCell(numCell.apply(String.valueOf(v.cantidadPedidos()), bg));
                    dt.addCell(numCell.apply("S/. " + v.total(), bg));
                    alt = !alt;
                }
                doc.add(dt);

            } else if ("menu-ranking".equalsIgnoreCase(reportType)) {
                List<MenuRankingDto> list = getMenuRanking(sucursalIds, fechaInicio, fechaFin);
                addSection.accept("Top 10 Platos Más Vendidos");
                PdfPTable table = new PdfPTable(new float[]{0.5f, 3, 1.2f, 2});
                table.setWidthPercentage(100);
                for (String col : new String[]{"#", "Plato", "Cantidad", "Ingresos (S/.)"}) table.addCell(hdrCell.apply(col));
                int idx = 1; boolean alt = false;
                for (MenuRankingDto r : list) {
                    java.awt.Color bg = alt ? ROW_ALT : java.awt.Color.WHITE;
                    String badge = idx == 1 ? "1" : idx == 2 ? "2" : idx == 3 ? "3" : String.valueOf(idx);
                    table.addCell(bodyCell.apply(badge, bg));
                    table.addCell(bodyCell.apply(r.nombrePlato(), bg));
                    table.addCell(numCell.apply(String.valueOf(r.cantidad()), bg));
                    table.addCell(numCell.apply("S/. " + r.ingresos(), bg));
                    idx++; alt = !alt;
                }
                doc.add(table);

            } else if ("caja-audit".equalsIgnoreCase(reportType)) {
                List<CajaAuditDto> list = getCajaAudit(sucursalIds, fechaInicio, fechaFin);
                addSection.accept("Arqueo de Turnos de Caja");
                PdfPTable table = new PdfPTable(new float[]{2, 2, 1.8f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});
                table.setWidthPercentage(100);
                for (String col : new String[]{"Sucursal", "Usuario", "Apertura", "Inicial", "Ingresos", "Egresos", "Esperado", "Real"}) {
                    table.addCell(hdrCell.apply(col));
                }
                boolean alt = false;
                for (CajaAuditDto c : list) {
                    java.awt.Color bg = alt ? ROW_ALT : java.awt.Color.WHITE;
                    boolean discrepancy = c.diferencia().compareTo(BigDecimal.ZERO) != 0;
                    java.awt.Color realBg = discrepancy ? DANGER_BG : SUCCESS_BG;
                    table.addCell(bodyCell.apply(c.sucursalNombre(), bg));
                    table.addCell(bodyCell.apply(c.usuarioNombre(), bg));
                    table.addCell(bodyCell.apply(c.fechaApertura().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")), bg));
                    table.addCell(numCell.apply("S/. " + c.montoInicial(), bg));
                    table.addCell(numCell.apply("S/. " + c.ingresosCajaChica(), bg));
                    table.addCell(numCell.apply("S/. " + c.egresosCajaChica(), bg));
                    table.addCell(numCell.apply("S/. " + c.saldoEsperado(), bg));
                    table.addCell(numCell.apply("S/. " + c.saldoReal(), realBg));
                    alt = !alt;
                }
                doc.add(table);

            } else if ("stock-critical".equalsIgnoreCase(reportType)) {
                StockCriticalDto dto = getStockCritical(sucursalIds);

                addSection.accept("Valoración Total del Inventario");
                PdfPTable kpi = new PdfPTable(1);
                kpi.setWidthPercentage(45);
                kpi.setSpacingAfter(14);
                PdfPCell kpiCell = new PdfPCell(new Phrase("S/. " + dto.valoracionTotalInventario(), fKpi));
                kpiCell.setBackgroundColor(BRAND_LIGHT);
                kpiCell.setPadding(12);
                kpiCell.setBorderColor(BORDER_CLR);
                kpiCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                kpi.addCell(kpiCell);
                doc.add(kpi);

                addSection.accept("Productos Bajo Stock Mínimo");
                PdfPTable table = new PdfPTable(new float[]{2.5f, 2, 1.2f, 1.2f, 1.2f});
                table.setWidthPercentage(100);
                for (String col : new String[]{"Producto", "Sucursal", "Stock Act.", "Stock Mín.", "Medida"}) table.addCell(hdrCell.apply(col));
                for (ProductoBajoStockDto p : dto.productosBajoStock()) {
                    double ratio = p.stockActual().doubleValue() / Math.max(p.stockMinimo().doubleValue(), 1);
                    java.awt.Color rowBg = ratio <= 0.25 ? DANGER_BG : WARNING_BG;
                    table.addCell(bodyCell.apply(p.nombreProducto(), rowBg));
                    table.addCell(bodyCell.apply(p.sucursalNombre(), rowBg));
                    table.addCell(numCell.apply(p.stockActual().toString(), rowBg));
                    table.addCell(numCell.apply(p.stockMinimo().toString(), rowBg));
                    table.addCell(bodyCell.apply(p.unidadMedida(), rowBg));
                }
                doc.add(table);

            } else if ("waiter-performance".equalsIgnoreCase(reportType)) {
                List<WaiterPerformanceDto> list = getWaiterPerformance(sucursalIds, fechaInicio, fechaFin);
                addSection.accept("Ranking de Desempeño del Personal");
                PdfPTable table = new PdfPTable(new float[]{0.5f, 3, 1.5f, 2});
                table.setWidthPercentage(100);
                for (String col : new String[]{"#", "Mozo / Personal", "Pedidos", "Total Vendido (S/.)"}) table.addCell(hdrCell.apply(col));
                int idx = 1; boolean alt = false;
                for (WaiterPerformanceDto w : list) {
                    java.awt.Color bg = alt ? ROW_ALT : java.awt.Color.WHITE;
                    table.addCell(bodyCell.apply(String.valueOf(idx), bg));
                    table.addCell(bodyCell.apply(w.waiterNombre(), bg));
                    table.addCell(numCell.apply(String.valueOf(w.cantidadPedidos()), bg));
                    table.addCell(numCell.apply("S/. " + w.totalVendido(), bg));
                    idx++; alt = !alt;
                }
                doc.add(table);
            }

            // ── Footer ───────────────────────────────────────────────────────
            doc.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("Documento generado automáticamente por NoirPos  •  " + now, fBodySm);
            footer.setAlignment(Element.ALIGN_RIGHT);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    // ─── EXCEL ──────────────────────────────────────────────────────────────────

    @Override
    public byte[] exportToExcel(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Color helper ─────────────────────────────────────────────────
            java.util.function.Function<int[], org.apache.poi.xssf.usermodel.XSSFColor> rgb = arr ->
                new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte) arr[0], (byte) arr[1], (byte) arr[2]}, null);

            // ── Title style (dark background, large white bold) ──────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFillForegroundColor(rgb.apply(new int[]{24, 24, 27}));
            titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            titleStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            org.apache.poi.xssf.usermodel.XSSFFont tFont = wb.createFont();
            tFont.setBold(true); tFont.setFontHeightInPoints((short) 14);
            tFont.setColor(rgb.apply(new int[]{255, 255, 255}));
            titleStyle.setFont(tFont);

            // ── Meta style (dark, small gray) ────────────────────────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle metaStyle = wb.createCellStyle();
            metaStyle.setFillForegroundColor(rgb.apply(new int[]{24, 24, 27}));
            metaStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.xssf.usermodel.XSSFFont mFont = wb.createFont();
            mFont.setFontHeightInPoints((short) 9);
            mFont.setColor(rgb.apply(new int[]{161, 161, 170}));
            metaStyle.setFont(mFont);

            // ── Header style (indigo, white bold, centered) ──────────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle hStyle = wb.createCellStyle();
            hStyle.setFillForegroundColor(rgb.apply(new int[]{99, 102, 241}));
            hStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            hStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            hStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            hStyle.setBottomBorderColor(rgb.apply(new int[]{79, 82, 221}));
            org.apache.poi.xssf.usermodel.XSSFFont hFont = wb.createFont();
            hFont.setBold(true); hFont.setFontHeightInPoints((short) 10);
            hFont.setColor(rgb.apply(new int[]{255, 255, 255}));
            hStyle.setFont(hFont);

            // ── Body styles (alternating white / gray-50) ────────────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle bodyEven = wb.createCellStyle();
            bodyEven.setFillForegroundColor(rgb.apply(new int[]{255, 255, 255}));
            bodyEven.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            bodyEven.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            bodyEven.setBottomBorderColor(rgb.apply(new int[]{228, 228, 231}));

            org.apache.poi.xssf.usermodel.XSSFCellStyle bodyOdd = wb.createCellStyle();
            bodyOdd.setFillForegroundColor(rgb.apply(new int[]{249, 250, 251}));
            bodyOdd.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            bodyOdd.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            bodyOdd.setBottomBorderColor(rgb.apply(new int[]{228, 228, 231}));

            // ── Numeric styles (right-aligned) ───────────────────────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle numEven = wb.createCellStyle();
            numEven.cloneStyleFrom(bodyEven);
            numEven.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

            org.apache.poi.xssf.usermodel.XSSFCellStyle numOdd = wb.createCellStyle();
            numOdd.cloneStyleFrom(bodyOdd);
            numOdd.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

            // ── Status-aware styles ──────────────────────────────────────────
            org.apache.poi.xssf.usermodel.XSSFCellStyle dangerStyle = wb.createCellStyle();
            dangerStyle.setFillForegroundColor(rgb.apply(new int[]{254, 242, 242}));
            dangerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            dangerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);
            dangerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dangerStyle.setBottomBorderColor(rgb.apply(new int[]{228, 228, 231}));

            org.apache.poi.xssf.usermodel.XSSFCellStyle successStyle = wb.createCellStyle();
            successStyle.setFillForegroundColor(rgb.apply(new int[]{240, 253, 244}));
            successStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            successStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);
            successStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            successStyle.setBottomBorderColor(rgb.apply(new int[]{228, 228, 231}));

            DateTimeFormatter dtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String now = LocalDateTime.now().format(dtFmt);

            // ── Write banner (returns next row index after meta rows) ─────────
            java.util.function.BiFunction<Sheet, String, int[]> writeBanner = (sheet, title) -> {
                Row r0 = sheet.createRow(0);
                r0.setHeightInPoints(28);
                Cell c0 = r0.createCell(0);
                c0.setCellValue(title);
                c0.setCellStyle(titleStyle);

                Row r1 = sheet.createRow(1);
                r1.setHeightInPoints(15);
                Cell c1 = r1.createCell(0);
                c1.setCellValue("Generado: " + now);
                c1.setCellStyle(metaStyle);
                return new int[]{3}; // header row at index 3
            };

            // ── Write column headers ─────────────────────────────────────────
            java.util.function.BiConsumer<Row, String[]> writeHdr = (row, cols) -> {
                row.setHeightInPoints(20);
                for (int i = 0; i < cols.length; i++) {
                    Cell c = row.createCell(i);
                    c.setCellValue(cols[i]);
                    c.setCellStyle(hStyle);
                }
            };

            // ── Auto-size columns ────────────────────────────────────────────
            java.util.function.BiConsumer<Sheet, Integer> autoSize = (sheet, count) -> {
                for (int i = 0; i < count; i++) sheet.autoSizeColumn(i);
            };

            // ── Report content ───────────────────────────────────────────────
            if ("sales".equalsIgnoreCase(reportType)) {
                SalesSummaryDto dto = getSalesSummary(sucursalIds, fechaInicio, fechaFin);

                Sheet s = wb.createSheet("Resumen");
                writeBanner.apply(s, "Ventas y Facturación");
                Row hr = s.createRow(3);
                writeHdr.accept(hr, new String[]{"Concepto", "Valor (S/.)"});

                Object[][] kpiData = {
                    {"Total Ventas",    dto.totalVentas().doubleValue()},
                    {"Ticket Promedio", dto.promedioTicket().doubleValue()},
                    {"Efectivo",        dto.totalEfectivo().doubleValue()},
                    {"Tarjeta",         dto.totalTarjeta().doubleValue()},
                    {"Yape",            dto.totalYape().doubleValue()},
                    {"Plin",            dto.totalPlin().doubleValue()},
                    {"Otros",           dto.totalOtros().doubleValue()},
                };
                boolean alt = false;
                int ri = 4;
                for (Object[] kRow : kpiData) {
                    Row r = s.createRow(ri++);
                    Cell cL = r.createCell(0); cL.setCellValue((String) kRow[0]); cL.setCellStyle(alt ? bodyOdd : bodyEven);
                    Cell cR = r.createCell(1); cR.setCellValue((double) kRow[1]); cR.setCellStyle(alt ? numOdd : numEven);
                    alt = !alt;
                }
                autoSize.accept(s, 2);

                Sheet ds = wb.createSheet("Ventas Diarias");
                writeBanner.apply(ds, "Detalle de Ventas Diarias");
                Row dhr = ds.createRow(3);
                writeHdr.accept(dhr, new String[]{"Fecha", "Pedidos", "Total (S/.)"});
                alt = false; ri = 4;
                for (VentaDiariaDto v : dto.ventasDiarias()) {
                    Row r = ds.createRow(ri++);
                    Cell c0 = r.createCell(0); c0.setCellValue(v.fecha().format(dtDate)); c0.setCellStyle(alt ? bodyOdd : bodyEven);
                    Cell c1 = r.createCell(1); c1.setCellValue(v.cantidadPedidos()); c1.setCellStyle(alt ? numOdd : numEven);
                    Cell c2 = r.createCell(2); c2.setCellValue(v.total().doubleValue()); c2.setCellStyle(alt ? numOdd : numEven);
                    alt = !alt;
                }
                autoSize.accept(ds, 3);

            } else if ("menu-ranking".equalsIgnoreCase(reportType)) {
                List<MenuRankingDto> list = getMenuRanking(sucursalIds, fechaInicio, fechaFin);
                Sheet s = wb.createSheet("Ranking Menú");
                writeBanner.apply(s, "Ranking del Menú — Top 10");
                Row hr = s.createRow(3);
                writeHdr.accept(hr, new String[]{"#", "Plato", "Cantidad", "Ingresos (S/.)"});
                int idx = 1; boolean alt = false; int ri = 4;
                for (MenuRankingDto r : list) {
                    Row row = s.createRow(ri++);
                    row.createCell(0).setCellValue(idx); row.getCell(0).setCellStyle(alt ? bodyOdd : bodyEven);
                    row.createCell(1).setCellValue(r.nombrePlato()); row.getCell(1).setCellStyle(alt ? bodyOdd : bodyEven);
                    row.createCell(2).setCellValue(r.cantidad()); row.getCell(2).setCellStyle(alt ? numOdd : numEven);
                    row.createCell(3).setCellValue(r.ingresos().doubleValue()); row.getCell(3).setCellStyle(alt ? numOdd : numEven);
                    idx++; alt = !alt;
                }
                autoSize.accept(s, 4);

            } else if ("caja-audit".equalsIgnoreCase(reportType)) {
                List<CajaAuditDto> list = getCajaAudit(sucursalIds, fechaInicio, fechaFin);
                Sheet s = wb.createSheet("Arqueo de Caja");
                writeBanner.apply(s, "Arqueo y Cierre de Caja");
                Row hr = s.createRow(3);
                writeHdr.accept(hr, new String[]{"Sucursal", "Usuario", "Apertura", "Cierre", "Inicial", "Ingresos", "Egresos", "Esperado", "Real", "Diferencia", "Estado"});
                boolean alt = false; int ri = 4;
                for (CajaAuditDto c : list) {
                    Row row = s.createRow(ri++);
                    boolean disc = c.diferencia().compareTo(BigDecimal.ZERO) != 0;
                    org.apache.poi.ss.usermodel.CellStyle bStyle = alt ? bodyOdd : bodyEven;
                    org.apache.poi.ss.usermodel.CellStyle nStyle = alt ? numOdd  : numEven;
                    org.apache.poi.ss.usermodel.CellStyle dStyle = disc ? dangerStyle : successStyle;

                    row.createCell(0).setCellValue(c.sucursalNombre()); row.getCell(0).setCellStyle(bStyle);
                    row.createCell(1).setCellValue(c.usuarioNombre());  row.getCell(1).setCellStyle(bStyle);
                    row.createCell(2).setCellValue(c.fechaApertura().format(dtFmt)); row.getCell(2).setCellStyle(bStyle);
                    row.createCell(3).setCellValue(c.fechaCierre() != null ? c.fechaCierre().format(dtFmt) : "—"); row.getCell(3).setCellStyle(bStyle);
                    row.createCell(4).setCellValue(c.montoInicial().doubleValue());      row.getCell(4).setCellStyle(nStyle);
                    row.createCell(5).setCellValue(c.ingresosCajaChica().doubleValue()); row.getCell(5).setCellStyle(nStyle);
                    row.createCell(6).setCellValue(c.egresosCajaChica().doubleValue());  row.getCell(6).setCellStyle(nStyle);
                    row.createCell(7).setCellValue(c.saldoEsperado().doubleValue());     row.getCell(7).setCellStyle(nStyle);
                    row.createCell(8).setCellValue(c.saldoReal().doubleValue());         row.getCell(8).setCellStyle(dStyle);
                    row.createCell(9).setCellValue(c.diferencia().doubleValue());        row.getCell(9).setCellStyle(dStyle);
                    row.createCell(10).setCellValue(c.estado());                         row.getCell(10).setCellStyle(bStyle);
                    alt = !alt;
                }
                autoSize.accept(s, 11);

            } else if ("stock-critical".equalsIgnoreCase(reportType)) {
                StockCriticalDto dto = getStockCritical(sucursalIds);
                Sheet s = wb.createSheet("Stock Crítico");
                writeBanner.apply(s, "Alertas de Stock Crítico");

                // KPI valuation row
                org.apache.poi.xssf.usermodel.XSSFCellStyle kpiStyle = wb.createCellStyle();
                kpiStyle.setFillForegroundColor(rgb.apply(new int[]{237, 233, 254}));
                kpiStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.xssf.usermodel.XSSFFont kpiFont = wb.createFont();
                kpiFont.setBold(true); kpiFont.setFontHeightInPoints((short) 11);
                kpiFont.setColor(rgb.apply(new int[]{99, 102, 241}));
                kpiStyle.setFont(kpiFont);

                Row kRow = s.createRow(3);
                kRow.createCell(0).setCellValue("Valoración Total del Inventario:"); kRow.getCell(0).setCellStyle(kpiStyle);
                kRow.createCell(1).setCellValue(dto.valoracionTotalInventario().doubleValue()); kRow.getCell(1).setCellStyle(kpiStyle);

                Row hr = s.createRow(5);
                writeHdr.accept(hr, new String[]{"Producto", "Sucursal", "Stock Actual", "Stock Mínimo", "Medida"});
                int ri = 6;
                for (ProductoBajoStockDto p : dto.productosBajoStock()) {
                    double ratio = p.stockActual().doubleValue() / Math.max(p.stockMinimo().doubleValue(), 1);
                    org.apache.poi.xssf.usermodel.XSSFCellStyle rowBg = wb.createCellStyle();
                    rowBg.setFillForegroundColor(ratio <= 0.25
                        ? rgb.apply(new int[]{254, 242, 242})
                        : rgb.apply(new int[]{255, 247, 237}));
                    rowBg.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                    rowBg.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                    rowBg.setBottomBorderColor(rgb.apply(new int[]{228, 228, 231}));

                    Row row = s.createRow(ri++);
                    for (int i = 0; i < 5; i++) { row.createCell(i).setCellStyle(rowBg); }
                    row.getCell(0).setCellValue(p.nombreProducto());
                    row.getCell(1).setCellValue(p.sucursalNombre());
                    row.getCell(2).setCellValue(p.stockActual().doubleValue());
                    row.getCell(3).setCellValue(p.stockMinimo().doubleValue());
                    row.getCell(4).setCellValue(p.unidadMedida());
                }
                autoSize.accept(s, 5);

            } else if ("waiter-performance".equalsIgnoreCase(reportType)) {
                List<WaiterPerformanceDto> list = getWaiterPerformance(sucursalIds, fechaInicio, fechaFin);
                Sheet s = wb.createSheet("Desempeño Personal");
                writeBanner.apply(s, "Desempeño del Personal");
                Row hr = s.createRow(3);
                writeHdr.accept(hr, new String[]{"#", "Mozo / Personal", "Pedidos", "Total Vendido (S/.)"});
                int idx = 1; boolean alt = false; int ri = 4;
                for (WaiterPerformanceDto w : list) {
                    Row row = s.createRow(ri++);
                    row.createCell(0).setCellValue(idx); row.getCell(0).setCellStyle(alt ? bodyOdd : bodyEven);
                    row.createCell(1).setCellValue(w.waiterNombre()); row.getCell(1).setCellStyle(alt ? bodyOdd : bodyEven);
                    row.createCell(2).setCellValue(w.cantidadPedidos()); row.getCell(2).setCellStyle(alt ? numOdd : numEven);
                    row.createCell(3).setCellValue(w.totalVendido().doubleValue()); row.getCell(3).setCellStyle(alt ? numOdd : numEven);
                    idx++; alt = !alt;
                }
                autoSize.accept(s, 4);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }
}
