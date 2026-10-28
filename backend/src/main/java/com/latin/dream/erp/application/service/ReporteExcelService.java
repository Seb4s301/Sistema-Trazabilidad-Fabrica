package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.entity.Orden;
import com.latin.dream.erp.domain.entity.PrecioComision;
import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.domain.repository.PrecioComisionRepository;
import org.apache.commons.lang3.StringUtils;
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

    private final PrecioComisionRepository precioComisionRepository;
    private final OrdenRepository ordenRepository;

    public ReporteExcelService(PrecioComisionRepository precioComisionRepository,
                               OrdenRepository ordenRepository) {
        this.precioComisionRepository = precioComisionRepository;
        this.ordenRepository = ordenRepository;
    }

    /**
     * Genera el Reporte de Comisiones por Producto en formato XLSX con Apache POI.
     */
    public byte[] exportarReporteComisiones() {
        log.info("Iniciando generación de reporte Excel de comisiones con Apache POI...");

        List<PrecioComision> precios = precioComisionRepository.findAll();

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
            titleCell.setCellValue("LATIN DREAM - REPORTE DE COMISIONES POR PRODUCTO");
            titleCell.setCellStyle(titleStyle);

            Row subtitleRow = sheet.createRow(1);
            Cell subCell = subtitleRow.createCell(0);
            subCell.setCellValue("Fecha de exportación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Encabezados de tabla
            String[] headers = {"#", "Tipo", "Modelo", "Categoría / Medida", "Relleno", "Unidades", "Precio Base ($)", "% Comisión", "Total Comisión ($)", "Estado"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int totalUnidades = 0;
            double totalComision = 0.0;
            int[] demoUnidades = {120, 200, 80, 60, 150, 90};
            String[] estados = {"Pagado", "Pendiente", "Pendiente", "En revisión", "Pagado", "Pagado"};

            for (int i = 0; i < precios.size(); i++) {
                PrecioComision pc = precios.get(i);
                Row row = sheet.createRow(rowIdx++);

                int u = i < demoUnidades.length ? demoUnidades[i] : 100;
                double base = pc.getPrecioBase().doubleValue();
                double pct = pc.getPorcentaje().doubleValue();
                double comision = (u * base * pct) / 100.0;
                String est = i < estados.length ? estados[i] : "Pendiente";

                totalUnidades += u;
                totalComision += comision;

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue("Modelo");
                row.createCell(2).setCellValue(StringUtils.defaultIfBlank(pc.getModelo().getNombre(), "Sin Modelo"));
                row.createCell(3).setCellValue(StringUtils.capitalize(StringUtils.defaultIfBlank(pc.getCategoria(), "General")));
                row.createCell(4).setCellValue(StringUtils.defaultIfBlank(pc.getModelo().getRelleno(), "Estándar"));
                row.createCell(5).setCellValue(u);

                Cell cellBase = row.createCell(6);
                cellBase.setCellValue(base);
                cellBase.setCellStyle(currencyStyle);

                row.createCell(7).setCellValue(pct + " %");

                Cell cellCom = row.createCell(8);
                cellCom.setCellValue(comision);
                cellCom.setCellStyle(currencyStyle);

                row.createCell(9).setCellValue(est);
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
            cellTotalM.setCellValue(totalComision);
            cellTotalM.setCellStyle(currencyStyle);

            // Auto-ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Reporte Excel generado exitosamente: {} filas procesadas.", precios.size());
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar reporte Excel con Apache POI: {}", e.getMessage(), e);
            throw new RuntimeException("Error en generación de reporte Excel", e);
        }
    }
}
