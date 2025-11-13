package com.backend.geopacking.service.impl;

import com.backend.geopacking.model.Cliente;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ClientExcelReport {
    private static final String[] HEADERS = {
            "ID", "Tipo Cliente", "Tipo Doc.", "Nro. Documento", "Nombre/Razón Social",
            "Nombre Comercial", "Teléfono", "Email", "País", "Departamento",
            "Provincia", "Distrito", "Dirección"
    };

    public ByteArrayInputStream clientesToExcel(List<Cliente> clientes) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet("Clientes");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Cliente cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(cliente.getId());
                row.createCell(1).setCellValue(cliente.getTipoCliente());
                row.createCell(2).setCellValue(cliente.getTipoDocumento());
                row.createCell(3).setCellValue(cliente.getNumeroDocumento());
                row.createCell(4).setCellValue(cliente.getNombre());
                row.createCell(5).setCellValue(cliente.getNombreComercial());
                row.createCell(6).setCellValue(cliente.getTelefono());
                row.createCell(7).setCellValue(cliente.getEmail());
                row.createCell(8).setCellValue(cliente.getPais());
                row.createCell(9).setCellValue(cliente.getDepartamento());
                row.createCell(10).setCellValue(cliente.getProvincia());
                row.createCell(11).setCellValue(cliente.getDistrito());
                row.createCell(12).setCellValue(cliente.getDireccion());
            }

            for(int i=0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
