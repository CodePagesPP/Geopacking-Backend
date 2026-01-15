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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import com.itextpdf.text.pdf.Barcode128; // Necesario para codigo de barras
import com.itextpdf.text.pdf.PdfContentByte;
import java.time.LocalTime; // Para la lógica de turno
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public ResponseEntity<Page<BobinaHistorialDTO>> listarHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime fechaInicio = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime fechaFin = (fin != null) ? fin.atTime(LocalTime.MAX) : null;


        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(bobinaRepository.buscarHistorialPaginado(fechaInicio, fechaFin, pageable));
    }



    @GetMapping("/reporte-pdf/{id}")
    public ResponseEntity<byte[]> generarReporteIndividual(@PathVariable Long id) {
        try {
            BobinaHistorialDTO bobina = bobinaRepository.obtenerBobinaPorId(id)
                    .orElseThrow(() -> new RuntimeException("Bobina no encontrada"));


            List<BobinaHistorialDTO> lista = Collections.singletonList(bobina);

            byte[] pdfBytes = generarPdfDisenoImagen(lista);

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



    private byte[] generarPdfDisenoImagen(List<BobinaHistorialDTO> lista) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();


        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();


        if (lista == null || lista.isEmpty()) {
            document.add(new Paragraph("No hay datos para mostrar."));
            document.close();
            return out.toByteArray();
        }



        for (BobinaHistorialDTO bobina : lista) {


            agregarEtiquetaBobina(writer, document, bobina);
        }

        document.close();
        return out.toByteArray();
    }



    private void agregarEtiquetaBobina(PdfWriter writer, Document document, BobinaHistorialDTO bobina) throws DocumentException {

        Font fontHeaderGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 11);


        String valCodigoProducto = bobina.getCodigoProducto() != null ? bobina.getCodigoProducto() : "SIN CÓDIGO";


        String valNombreProducto = bobina.getNombreProducto() != null ? bobina.getNombreProducto() : "PRODUCTO";


        String valCodigoOT = bobina.getCodigoOT() != null ? bobina.getCodigoOT() : "-";

        String valCodigoBobina = bobina.getCodigoBobina() != null ? bobina.getCodigoBobina() : "-";


        String valPesoBruto = String.format("%.2f Kg", bobina.getPesoBruto() != null ? bobina.getPesoBruto() : 0.0);
        String valPesoNeto = String.format("%.2f Kg", bobina.getPesoNeto() != null ? bobina.getPesoNeto() : 0.0);


        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String valFecha = LocalDate.now().format(formatterFecha);

        String valTurno = "2";
        LocalTime horaActual = LocalTime.now();
        if (horaActual.getHour() >= 7 && horaActual.getHour() < 19) {
            valTurno = "1";
        } else {
            valTurno = "2";
        }


        String valMaquina = bobina.getCodigoMaquina() != null ? bobina.getCodigoMaquina() : "MAQ-X";
        String valOperador = bobina.getOperador() != null ? bobina.getOperador() : "OPERADOR";


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setKeepTogether(true);
        table.setSpacingAfter(25f);
        table.setWidths(new float[]{1f, 1f, 1f, 1f});

        // Fila 1 & 2: Producto
        PdfPCell cellCodProd = new PdfPCell(new Phrase(valCodigoProducto, fontHeaderGrande));
        cellCodProd.setColspan(4);
        cellCodProd.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCodProd.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellCodProd.setPadding(6f);
        table.addCell(cellCodProd);

        PdfPCell cellNomProd = new PdfPCell(new Phrase(valNombreProducto, fontHeaderGrande));
        cellNomProd.setColspan(4);
        cellNomProd.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellNomProd.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellNomProd.setPadding(6f);
        table.addCell(cellNomProd);

        // Datos
        addCellHeader(table, "CÓDIGO OT", fontLabel);
        addCellHeader(table, "CÓDIGO BOBINA", fontLabel);
        addCellHeader(table, "PESO BRUTO", fontLabel);
        addCellHeader(table, "PESO NETO", fontLabel);

        addCellValue(table, valCodigoOT, fontValue);
        addCellValue(table, valCodigoBobina, fontValue);
        addCellValue(table, valPesoBruto, fontValue);
        addCellValue(table, valPesoNeto, fontValue);

        addCellHeader(table, "FECHA", fontLabel);
        addCellHeader(table, "TURNO", fontLabel);
        addCellHeader(table, "MÁQUINA", fontLabel);
        addCellHeader(table, "OPERADOR", fontLabel);

        addCellValue(table, valFecha, fontValue);
        addCellValue(table, valTurno, fontValue);
        addCellValue(table, valMaquina, fontValue);
        addCellValue(table, valOperador, fontValue);


        PdfPCell cellBarcode = new PdfPCell();
        cellBarcode.setColspan(4);
        cellBarcode.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellBarcode.setPadding(10f);
        cellBarcode.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellBarcode.setMinimumHeight(45f);

        try {
            PdfContentByte cb = writer.getDirectContent();
            Barcode128 code128 = new Barcode128();
            String codigoParaBarras = (valCodigoBobina != null && !valCodigoBobina.equals("-")) ? valCodigoBobina : "000000";
            code128.setCode(codigoParaBarras);
            code128.setCodeType(Barcode128.CODE128);
            code128.setBarHeight(35f);
            code128.setFont(null);

            com.itextpdf.text.Image image128 = code128.createImageWithBarcode(cb, null, null);
            cellBarcode.addElement(image128);
        } catch (Exception e) {
            cellBarcode.addElement(new Phrase("ERROR BARCODE"));
        }
        table.addCell(cellBarcode);

        document.add(table);
    }


    private void addCellHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addCellValue(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setMinimumHeight(15f);
        table.addCell(cell);
    }

    @GetMapping("/transito")
    public ResponseEntity<List<BobinaTransitoDTO>> obtenerBobinasEnTransito() {
        return ResponseEntity.ok(bobinaService.listarBobinasEnTransito());
    }

    @GetMapping("/reporte-tabla-pdf")
    public ResponseEntity<byte[]> generarReporteTabla(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        byte[] pdfBytes = bobinaService.generarReporteCompletoBobinasPdf(inicio, fin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Reporte_Bobinas_Stock.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
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
