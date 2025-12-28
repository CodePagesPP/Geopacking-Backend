package com.backend.geopacking.controller;

import com.backend.geopacking.dto.BobinaHistorialDTO;
import com.backend.geopacking.dto.BobinaTransitoDTO;
import com.backend.geopacking.repository.BobinaEXRepository;
import com.backend.geopacking.service.BobinaService;
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
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/bobinas")
public class BobinaController {

    @Autowired
    private BobinaEXRepository bobinaRepository;
    @Autowired
    private BobinaService bobinaService;


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
            String rangoFechas = "TODOS LOS TIEMPOS";

            if (inicio != null && fin != null) {
                LocalDateTime fechaInicio = inicio.atStartOfDay();
                LocalDateTime fechaFin = fin.atTime(LocalTime.MAX);
                lista = bobinaRepository.filtrarPorFechas(fechaInicio, fechaFin);
                rangoFechas = inicio.toString() + " al " + fin.toString();
            } else {
                lista = bobinaRepository.obtenerHistorialCompleto();
            }


            byte[] pdfBytes = generarPdfDisenoImagen(lista, "REPORTE DE STOCK", rangoFechas);

            return retornarPdf(pdfBytes, "Reporte_Stock.pdf");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/reporte-pdf/{id}")
    public ResponseEntity<byte[]> generarReporteIndividual(@PathVariable Long id) {
        try {
            BobinaHistorialDTO bobina = bobinaRepository.obtenerBobinaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Bobina no encontrada"));


            List<BobinaHistorialDTO> lista = Collections.singletonList(bobina);

            byte[] pdfBytes = generarPdfDisenoImagen(lista, "ETIQUETA INDIVIDUAL", "ID: " + id);

            return retornarPdf(pdfBytes, "Bobina_" + bobina.getCodigoBobina() + ".pdf");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private ResponseEntity<byte[]> retornarPdf(byte[] pdfBytes, String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


    private byte[] generarPdfDisenoImagen(List<BobinaHistorialDTO> lista, String tipoMovimiento, String documentoInfo) throws DocumentException {

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();


        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontCuerpoTabla = FontFactory.getFont(FontFactory.HELVETICA, 9);


        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2, 1});


        PdfPCell cellEmpty = new PdfPCell(new Phrase(""));
        cellEmpty.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cellEmpty);


        PdfPCell cellEmpresa = new PdfPCell(new Phrase("GEOPACKING S.A.C.", fontEmpresa));
        cellEmpresa.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cellEmpresa);


        String fechaImpresion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        PdfPCell cellSmall = new PdfPCell(new Phrase(fechaImpresion, fontSubtitulo));
        cellSmall.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSmall.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cellSmall);

        document.add(headerTable);
        document.add(new Paragraph(" "));


        PdfPTable infoTable = new PdfPTable(3);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 1f, 1f});


        infoTable.addCell(crearCeldaInfo("LOCAL:", "PLANTA PRINCIPAL", fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("FECHA:", fechaImpresion, fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("DOCUMENTO:", documentoInfo, fontLabel, fontValue));


        infoTable.addCell(crearCeldaInfo("TIPO DE MOVIMIENTO:", tipoMovimiento, fontLabel, fontValue));

        String usuario = lista.isEmpty() ? "-" : (lista.size() == 1 ? lista.get(0).getOperador() : "VARIOS / SISTEMA");
        infoTable.addCell(crearCeldaInfo("USUARIO:", usuario, fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("OPERACIÓN:", "CONTROL DE PISO", fontLabel, fontValue));

        document.add(infoTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("DETALLES DEL REPORTE", fontLabel));
        document.add(new Paragraph(" "));


        PdfPTable dataTable = new PdfPTable(5);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new float[]{1f, 2f, 2f, 3f, 6f});


        String[] headers = {"ITEM", "CANTIDAD", "UNIDAD", "COD PROD", "DESCRIPCIÓN (BOBINA / OT)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            cell.setBorderWidth(1.5f);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataTable.addCell(cell);
        }


        int itemCounter = 1;
        double totalPeso = 0;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (BobinaHistorialDTO b : lista) {

            addCellClean(dataTable, String.valueOf(itemCounter++), fontCuerpoTabla, Element.ALIGN_CENTER);


            addCellClean(dataTable, df.format(b.getPesoNeto()), fontCuerpoTabla, Element.ALIGN_CENTER);


            addCellClean(dataTable, "KG", fontCuerpoTabla, Element.ALIGN_CENTER);


            addCellClean(dataTable, b.getCodigoProducto(), fontCuerpoTabla, Element.ALIGN_CENTER);


            String desc = b.getCodigoBobina() + " - " + b.getOperacion();
            addCellClean(dataTable, desc, fontCuerpoTabla, Element.ALIGN_LEFT);

            totalPeso += b.getPesoNeto();
        }

        document.add(dataTable);


        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell cellFirma = new PdfPCell(new Phrase("----------------------------------------\nCONFIRMADO", fontValue));
        cellFirma.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellFirma.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(cellFirma);


        document.add(new Paragraph("Total Peso: " + df.format(totalPeso) + " KG", fontHeaderTabla));

        document.add(footerTable);

        document.close();
        return out.toByteArray();
    }




    private PdfPCell crearCeldaInfo(String label, String value, Font fLabel, Font fValue) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", fLabel));
        phrase.add(new Chunk(value != null ? value : "-", fValue));

        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }


    private void addCellClean(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        // Opcional: cell.setBorder(Rectangle.BOTTOM); // Si quieres líneas tenues entre filas
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        table.addCell(cell);
    }

    @GetMapping("/transito")
    public ResponseEntity<List<BobinaTransitoDTO>> obtenerBobinasEnTransito() {
        return ResponseEntity.ok(bobinaService.listarBobinasEnTransito());
    }

    @GetMapping("/stock")
    public ResponseEntity<Double> obtenerStockTotal() {
        return ResponseEntity.ok(bobinaService.obtenerStockTotal());
    }

    @GetMapping("/conteo/{otId}")
    public ResponseEntity<Long> obtenerCorrelativoBobina(@PathVariable Long otId) {
        Long cantidad = bobinaRepository.contarBobinasPorOT(otId);
        return ResponseEntity.ok(cantidad);
    }
}
