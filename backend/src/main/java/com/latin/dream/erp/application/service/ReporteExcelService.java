package com.latin.dream.erp.application.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Servicio de Aplicación: Generación de reportes en hojas de cálculo Excel (.xlsx)
 * utilizando Apache POI y utilitarios de Apache Commons (lang3).
 *
 * Cumple con el Principio de Responsabilidad Única (SRP): Se enfoca exclusivamente
 * en la construcción y estilización de documentos tabulares.
 */
@Service
@Transactional(readOnly = true)
public class ReporteExcelService {

    private static final Logger log = LoggerFactory.getLogger(ReporteExcelService.class);

    private final ComisionesService comisionesService;

    public ReporteExcelService(ComisionesService comisionesService) {
        this.comisionesService = comisionesService;
    }

    /**
     * Genera el Reporte de Comisiones por Producto en formato XLSX con Apache POI.
     */
    public byte[] exportarReporteComisiones() { return exportarReporteComisiones(java.time.YearMonth.now()); }
    public byte[] exportarReporteComisiones(java.time.YearMonth periodo) {
        log.info("Iniciando generación de reporte Excel de comisiones con Apache POI...");

        List<Map<String, Object>> filas = comisionesService.calcularFilas(periodo);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Comisiones por Producto");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LATIN DREAM - COMISIONES: ESTIMACIONES, LIQUIDACIONES Y PAGOS");
            titleCell.setCellStyle(titleStyle);

            Row subtitleRow = sheet.createRow(1);
            Cell subCell = subtitleRow.createCell(0);
            subCell.setCellValue("Período " + periodo + " · Fecha de exportación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Encabezados de tabla
            String[] headers = {"#", "Orden", "Modelo", "Categoría / Medida", "Relleno", "Unidades", "Precio Base ($)", "% Comisión", "Total Comisión ($)", "Estado", "Referencia de pago", "Liquidado por", "Pagado por"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int totalUnidades = 0;
            BigDecimal totalComision = BigDecimal.ZERO;
            for (int i = 0; i < filas.size(); i++) {
                Map<String, Object> fila = filas.get(i);
                Row row = sheet.createRow(rowIdx++);
                int unidades = (Integer) fila.get("unidades");
                BigDecimal monto = (BigDecimal) fila.get("monto");
                totalUnidades += unidades;
                totalComision = totalComision.add(monto);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue((String) fila.get("tipo"));
                row.createCell(2).setCellValue((String) fila.get("modelo"));
                row.createCell(3).setCellValue((String) fila.get("categoria"));
                row.createCell(4).setCellValue((String) fila.get("relleno"));
                row.createCell(5).setCellValue(unidades);
                row.createCell(6).setCellValue(((BigDecimal) fila.get("precioBase")).doubleValue());
                row.getCell(6).setCellStyle(currencyStyle);
                row.createCell(7).setCellValue(fila.get("porcentaje").toString() + " %");
                row.createCell(8).setCellValue(monto.doubleValue());
                row.getCell(8).setCellStyle(currencyStyle);
                row.createCell(9).setCellValue((String) fila.get("estado"));
                row.createCell(10).setCellValue(java.util.Objects.toString(fila.get("referenciaPago"),""));
                row.createCell(11).setCellValue(java.util.Objects.toString(fila.get("liquidadoPor"),""));
                row.createCell(12).setCellValue(java.util.Objects.toString(fila.get("pagadoPor"),""));
            }

            // Fila de totales
            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell lblTotal = totalRow.createCell(4);
            lblTotal.setCellValue("TOTALES:");
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            lblTotal.setCellStyle(boldStyle);

            Cell cellTotalU = totalRow.createCell(5);
            cellTotalU.setCellValue(totalUnidades);
            cellTotalU.setCellStyle(boldStyle);

            Cell cellTotalM = totalRow.createCell(8);
            cellTotalM.setCellValue(totalComision.doubleValue());
            cellTotalM.setCellStyle(currencyStyle);

            // Auto-ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Reporte Excel generado exitosamente: {} filas procesadas.", filas.size());
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar reporte Excel con Apache POI: {}", e.getMessage(), e);
            throw new RuntimeException("Error en generación de reporte Excel", e);
        }
    }
}
