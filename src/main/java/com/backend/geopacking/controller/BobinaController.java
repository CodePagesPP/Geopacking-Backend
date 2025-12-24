package com.backend.geopacking.controller;

import com.backend.geopacking.dto.BobinaHistorialDTO;
import com.backend.geopacking.repository.BobinaEXRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/bobinas")
public class BobinaController {

    @Autowired
    private BobinaEXRepository bobinaRepository;


    @GetMapping("/historial")
    public ResponseEntity<List<BobinaHistorialDTO>> listarHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<BobinaHistorialDTO> lista;

        if (inicio != null && fin != null) {

            LocalDateTime fechaInicio = inicio.atStartOfDay();
            LocalDateTime fechaFin = fin.atTime(LocalTime.MAX);

            lista = bobinaRepository.filtrarPorFechas(fechaInicio, fechaFin);
        } else {
            lista = bobinaRepository.obtenerHistorialCompleto();
        }

        return ResponseEntity.ok(lista);
    }


    @GetMapping("/reporte-pdf")
    public ResponseEntity<byte[]> generarReporteStock(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        try {
            List<BobinaHistorialDTO> lista;

            if (inicio != null && fin != null) {
                LocalDateTime fechaInicio = inicio.atStartOfDay();
                LocalDateTime fechaFin = fin.atTime(LocalTime.MAX);
                lista = bobinaRepository.filtrarPorFechas(fechaInicio, fechaFin);
            } else {
                lista = bobinaRepository.obtenerHistorialCompleto();
            }

            // Usamos la misma lógica de generación PDF que ya tenías
            byte[] pdfBytes = generarPdfStock(lista);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String nombreArchivo = (inicio != null) ? "Reporte_Stock_Filtrado.pdf" : "Reporte_Stock_Total.pdf";
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private byte[] generarPdfStock(List<BobinaHistorialDTO> lista) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);


        Paragraph titulo = new Paragraph("REPORTE DE STOCK DE BOBINAS", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));


        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        table.setWidths(new float[]{3f, 3f, 3f, 3f, 4f, 2f, 2f});


        String[] headers = {"Fecha", "Cód. Bobina", "Operación", "Producto", "Operador", "Bruto", "Neto"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        double totalNeto = 0;


        for (BobinaHistorialDTO b : lista) {
            table.addCell(new Phrase(b.getFecha() != null ? b.getFecha().format(dtf) : "-", dataFont));
            table.addCell(new Phrase(b.getCodigoBobina(), dataFont));
            table.addCell(new Phrase(b.getOperacion(), dataFont));
            table.addCell(new Phrase(b.getCodigoProducto(), dataFont));
            table.addCell(new Phrase(b.getOperador(), dataFont));
            table.addCell(new Phrase(String.valueOf(b.getPesoBruto()), dataFont));
            table.addCell(new Phrase(String.valueOf(b.getPesoNeto()), dataFont));

            totalNeto += b.getPesoNeto();
        }

        document.add(table);

        document.add(new Paragraph(" "));
        Paragraph totalP = new Paragraph("Total Peso Neto en Stock: " + String.format("%.2f Kg", totalNeto), tituloFont);
        totalP.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalP);

        document.close();
        return out.toByteArray();
    }
}
