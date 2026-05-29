package com.restaurante.resturante.service.venta.jpa;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.domain.ventas.MovimientoCaja;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.repository.venta.CajaTurnoRepository;
import com.restaurante.resturante.repository.venta.MovimientoCajaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CajaReporteService {

    private final CajaTurnoRepository cajaRepository;
    private final CajaTurnoService cajaTurnoService;
    private final MovimientoCajaRepository movimientoRepository;

    public byte[] generarReportePDF(String cajaId) {
        CajaTurno caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new RuntimeException("CAJA NO ENCONTRADA"));

        CajaResumentDto resumen = cajaTurnoService.obtenerResumenArqueo(cajaId);
        List<MovimientoCaja> movimientos = movimientoRepository.findByCajaTurnoIdOrderByFechaDesc(cajaId);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fuentes
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            // Título
            Paragraph title = new Paragraph("REPORTE DE ARQUEO Y CIERRE DE CAJA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información General
            document.add(new Paragraph("INFORMACIÓN GENERAL DEL TURNO", sectionFont));
            document.add(new Paragraph(" ", valueFont)); // Espacio

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addCell(infoTable, "Código de Apertura:", labelFont);
            addCell(infoTable, caja.getCodigoApertura(), valueFont);
            addCell(infoTable, "Usuario / Cajero:", labelFont);
            addCell(infoTable, caja.getUser().getUsername(), valueFont);
            addCell(infoTable, "Sucursal:", labelFont);
            addCell(infoTable, caja.getSucursal().getNombre(), valueFont);
            addCell(infoTable, "Estado Turno:", labelFont);
            addCell(infoTable, caja.getEstado(), valueFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            addCell(infoTable, "Fecha Apertura:", labelFont);
            addCell(infoTable, caja.getFechaApertura().format(formatter), valueFont);
            addCell(infoTable, "Fecha Cierre:", labelFont);
            addCell(infoTable, caja.getFechaCierre() != null ? caja.getFechaCierre().format(formatter) : "Turno Activo", valueFont);

            document.add(infoTable);

            // Resumen de Arqueo
            document.add(new Paragraph("RESUMEN DE SALDOS (EFECTIVO Y VIRTUAL)", sectionFont));
            document.add(new Paragraph(" ", valueFont)); // Espacio

            PdfPTable saldosTable = new PdfPTable(3);
            saldosTable.setWidthPercentage(100);
            saldosTable.setSpacingAfter(15);

            // Encabezado tabla saldos
            saldosTable.addCell(new Phrase("Concepto", labelFont));
            saldosTable.addCell(new Phrase("Esperado (Sistema)", labelFont));
            saldosTable.addCell(new Phrase("Real (Contado)", labelFont));

            BigDecimal cashApertura = caja.getMontoAperturaEfectivo() != null ? caja.getMontoAperturaEfectivo() : caja.getMontoApertura();
            BigDecimal virtualApertura = caja.getMontoAperturaVirtual() != null ? caja.getMontoAperturaVirtual() : BigDecimal.ZERO;

            saldosTable.addCell(new Phrase("Apertura (Base) Efectivo", valueFont));
            saldosTable.addCell(new Phrase("S/ " + cashApertura, valueFont));
            saldosTable.addCell(new Phrase("S/ " + cashApertura, valueFont));

            saldosTable.addCell(new Phrase("Apertura (Base) Virtual", valueFont));
            saldosTable.addCell(new Phrase("S/ " + virtualApertura, valueFont));
            saldosTable.addCell(new Phrase("S/ " + virtualApertura, valueFont));

            saldosTable.addCell(new Phrase("Ventas en Efectivo", valueFont));
            saldosTable.addCell(new Phrase("S/ " + resumen.totalVentasEfectivo(), valueFont));
            saldosTable.addCell(new Phrase("-", valueFont));

            saldosTable.addCell(new Phrase("Ventas en Tarjeta / Virtual", valueFont));
            saldosTable.addCell(new Phrase("S/ " + resumen.totalVentasTarjeta(), valueFont));
            saldosTable.addCell(new Phrase("-", valueFont));

            saldosTable.addCell(new Phrase("Movimientos Caja Chica (Ingresos)", valueFont));
            saldosTable.addCell(new Phrase("S/ " + resumen.totalIngresosCajaChica(), valueFont));
            saldosTable.addCell(new Phrase("-", valueFont));

            // Entrada Caja Chica (Ventas + Ingresos Manuales)
            BigDecimal totalVentas = resumen.totalVentasGlobal() != null ? resumen.totalVentasGlobal() : BigDecimal.ZERO;
            BigDecimal totalIngresos = resumen.totalIngresosCajaChica() != null ? resumen.totalIngresosCajaChica() : BigDecimal.ZERO;
            BigDecimal totalEntradas = totalVentas.add(totalIngresos);
            saldosTable.addCell(new Phrase("ENTRADA CAJA CHICA (Ventas + Ingresos)", labelFont));
            saldosTable.addCell(new Phrase("S/ " + totalEntradas, labelFont));
            saldosTable.addCell(new Phrase("-", valueFont));

            saldosTable.addCell(new Phrase("Movimientos Caja Chica (Egresos)", valueFont));
            saldosTable.addCell(new Phrase("S/ " + resumen.totalEgresosCajaChica(), valueFont));
            saldosTable.addCell(new Phrase("-", valueFont));

            // Totales por flujo
            BigDecimal cashEsperado = resumen.saldoEsperadoEnCaja();
            BigDecimal cashReal = caja.getMontoCierreRealEfectivo() != null ? caja.getMontoCierreRealEfectivo() : BigDecimal.ZERO;
            saldosTable.addCell(new Phrase("TOTAL EFECTIVO EN CAJA", labelFont));
            saldosTable.addCell(new Phrase("S/ " + cashEsperado, labelFont));
            saldosTable.addCell(new Phrase("S/ " + cashReal, labelFont));

            BigDecimal cardEsperado = resumen.saldoEsperadoVirtual() != null ? resumen.saldoEsperadoVirtual() : BigDecimal.ZERO;
            BigDecimal cardReal = caja.getMontoCierreRealVirtual() != null ? caja.getMontoCierreRealVirtual() : BigDecimal.ZERO;
            saldosTable.addCell(new Phrase("TOTAL VIRTUAL/TARJETA", labelFont));
            saldosTable.addCell(new Phrase("S/ " + cardEsperado, labelFont));
            saldosTable.addCell(new Phrase("S/ " + cardReal, labelFont));

            // Diferencia general
            BigDecimal totalEsperadoGlobal = cashEsperado.add(cardEsperado);
            BigDecimal totalRealGlobal = cashReal.add(cardReal);
            saldosTable.addCell(new Phrase("ARQUEO GLOBAL (EFECTIVO + VIRTUAL)", labelFont));
            saldosTable.addCell(new Phrase("S/ " + totalEsperadoGlobal, labelFont));
            saldosTable.addCell(new Phrase("S/ " + totalRealGlobal, labelFont));

            document.add(saldosTable);

            Paragraph diffParagraph = new Paragraph("Diferencia Arqueo: S/ " + (caja.getDiferencia() != null ? caja.getDiferencia() : BigDecimal.ZERO), labelFont);
            document.add(diffParagraph);

            Paragraph obsParagraph = new Paragraph("Observaciones: " + (caja.getObservaciones() != null ? caja.getObservaciones() : "Sin observaciones."), valueFont);
            obsParagraph.setSpacingAfter(20);
            document.add(obsParagraph);

            // Listado de Movimientos
            document.add(new Paragraph("HISTORIAL DE MOVIMIENTOS CAJA CHICA", sectionFont));
            document.add(new Paragraph(" ", valueFont)); // Espacio

            PdfPTable movsTable = new PdfPTable(4);
            movsTable.setWidthPercentage(100);

            movsTable.addCell(new Phrase("Fecha/Hora", labelFont));
            movsTable.addCell(new Phrase("Descripción", labelFont));
            movsTable.addCell(new Phrase("Tipo", labelFont));
            movsTable.addCell(new Phrase("Monto", labelFont));

            for (MovimientoCaja m : movimientos) {
                movsTable.addCell(new Phrase(m.getFecha().format(formatter), valueFont));
                movsTable.addCell(new Phrase(m.getDescripcion(), valueFont));
                movsTable.addCell(new Phrase(m.getTipo().toString(), valueFont));
                movsTable.addCell(new Phrase("S/ " + m.getMonto(), valueFont));
            }

            if (movimientos.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay movimientos registrados en este turno.", valueFont));
                emptyCell.setColspan(4);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                movsTable.addCell(emptyCell);
            }

            document.add(movsTable);

        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF de caja", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
    }
}
