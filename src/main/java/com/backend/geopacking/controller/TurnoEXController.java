package com.backend.geopacking.controller;

import com.backend.geopacking.dto.RegistroTurnoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.OrdenTrabajoEXRepository;
import com.backend.geopacking.repository.TurnoEXRepository;
import com.backend.geopacking.service.InsumoService;
import com.backend.geopacking.service.InventarioService;
import com.backend.geopacking.service.TurnoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/turno-ex")
public class TurnoEXController {

    @Autowired
    private TurnoEXRepository turnoRepository;


    @Autowired
    private TurnoEXService turnoService;

    @PostMapping("/finalizar")

    public ResponseEntity<byte[]> finalizarTurno(@RequestBody RegistroTurnoDTO dto) {
        try {

            TurnoEX turnoGuardado = turnoService.guardarTurno(dto);


            OrdenTrabajoEX ot = turnoGuardado.getOrdenTrabajo();


            byte[] pdfBytes = generarPdfInterno(turnoGuardado, ot, dto.getUsuarioNombre());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Resumen_" + ot.getCodigo() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private byte[] generarPdfInterno(TurnoEX turno, OrdenTrabajoEX ot, String usuarioResponsable) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();


        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);


        Paragraph titulo = new Paragraph("REPORTE DE FIN DE TURNO", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" ")); // Espacio


        document.add(new Paragraph("Código OT: " + ot.getCodigo(), boldFont));

        String infoMaquina = "N/A";
        if (ot.getMaquina() != null) {

            infoMaquina = ot.getMaquina().getCodigo();
        }
        document.add(new Paragraph("Máquina: " + infoMaquina, normalFont));

        document.add(new Paragraph("Operador Responsable: " + usuarioResponsable, normalFont));
        document.add(new Paragraph("------------------------------------------------", normalFont));

        String infoProducto = "N/A";
        if (ot.getProducto() != null) {

            infoProducto = ot.getProducto().getName();
        }
        document.add(new Paragraph("Producto: " + infoProducto, normalFont));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String fechaFormateada = "---";
        if (turno.getFechaHoraFin() != null) {
            fechaFormateada = turno.getFechaHoraFin().format(formatter);
        }

        document.add(new Paragraph("Fecha Fin: " + fechaFormateada, normalFont));
        document.add(new Paragraph(" "));


        if (turno.getBobinas() != null && !turno.getBobinas().isEmpty()) {
            document.add(new Paragraph("Bobinas Producidas:", boldFont));
            document.add(new Paragraph(" "));

            PdfPTable tablaBobinas = new PdfPTable(5);
            tablaBobinas.setWidthPercentage(100);


            addHeaderCell(tablaBobinas, "Código Bobina");
            addHeaderCell(tablaBobinas, "H. Inicio");
            addHeaderCell(tablaBobinas, "H. Fin");
            addHeaderCell(tablaBobinas, "Peso Bruto");
            addHeaderCell(tablaBobinas, "Peso Neto");


            for (BobinaEX b : turno.getBobinas()) {
                tablaBobinas.addCell(new Phrase(b.getCodigo() != null ? b.getCodigo() : "-", normalFont));
                String hIni = b.getHoraInicio() != null ? b.getHoraInicio() : "-";
                String hFin = b.getHoraFin() != null ? b.getHoraFin() : "-";
                tablaBobinas.addCell(new Phrase(hIni, normalFont));
                tablaBobinas.addCell(new Phrase(hFin, normalFont));
                tablaBobinas.addCell(new Phrase(String.valueOf(b.getPesoBruto()), normalFont));
                tablaBobinas.addCell(new Phrase(String.valueOf(b.getPesoNeto()), normalFont));
            }
            document.add(tablaBobinas);


            Paragraph totalP = new Paragraph("Total Kg Turno: " + turno.getTotalKilosProducidos(), boldFont);
            totalP.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalP);
        }

        document.add(new Paragraph(" "));


        if (turno.getMateriales() != null && !turno.getMateriales().isEmpty()) {
            document.add(new Paragraph("Materiales Utilizados:", boldFont));
            document.add(new Paragraph(" "));

            PdfPTable tablaMat = new PdfPTable(2);
            tablaMat.setWidthPercentage(100);

            addHeaderCell(tablaMat, "Material");
            addHeaderCell(tablaMat, "Cantidad (Kg)");

            for (MaterialEX m : turno.getMateriales()) {
                tablaMat.addCell(new Phrase(m.getNombre(), normalFont));
                tablaMat.addCell(new Phrase(String.valueOf(m.getCantidadKg()), normalFont));
            }
            document.add(tablaMat);
        }

        if (turno.getScrapps() != null && !turno.getScrapps().isEmpty()) {
            document.add(new Paragraph("Scrapp:", boldFont));
            document.add(new Paragraph(" "));

            PdfPTable tablaScrapp = new PdfPTable(2);
            tablaScrapp.setWidthPercentage(100);


            addHeaderCell(tablaScrapp, "Tipo");
            addHeaderCell(tablaScrapp, "Cantidad (Kg)");


            for (ScrappEX s : turno.getScrapps()) {
                tablaScrapp.addCell(new Phrase(s.getTipo(), normalFont));
                tablaScrapp.addCell(new Phrase(String.valueOf(s.getCantidad()), normalFont));
            }
            document.add(tablaScrapp);
        }





        document.add(new Paragraph(" "));


        double totalMaterialKg = 0;
        if (turno.getMateriales() != null) {
            totalMaterialKg = turno.getMateriales().stream()
                    .mapToDouble(MaterialEX::getCantidadKg).sum();
        }

        double totalScrappKg = 0;
        if (turno.getScrapps() != null) {
            totalScrappKg = turno.getScrapps().stream()
                    .mapToDouble(ScrappEX::getCantidad).sum();
        }


        double totalBalanceKg = totalMaterialKg + totalScrappKg;


        document.add(new Paragraph("Balance de Turno:", boldFont));
        document.add(new Paragraph(" "));

        PdfPTable tablaBalance = new PdfPTable(3);
        tablaBalance.setWidthPercentage(100);

        tablaBalance.setWidths(new float[]{4f, 2f, 2f});


        addHeaderCell(tablaBalance, "Ítem");
        addHeaderCell(tablaBalance, "Kilos");
        addHeaderCell(tablaBalance, "%");


        tablaBalance.addCell(new Phrase("Total Material", normalFont));
        tablaBalance.addCell(new Phrase(String.format("%.2f Kg", totalMaterialKg), normalFont));

        double porcMaterial = totalBalanceKg > 0 ? (totalMaterialKg / totalBalanceKg) * 100 : 0;
        tablaBalance.addCell(new Phrase(String.format("%.2f %%", porcMaterial), normalFont));


        tablaBalance.addCell(new Phrase("Total Scrapp", normalFont));
        tablaBalance.addCell(new Phrase(String.format("%.2f Kg", totalScrappKg), normalFont));

        double porcScrapp = totalBalanceKg > 0 ? (totalScrappKg / totalBalanceKg) * 100 : 0;
        tablaBalance.addCell(new Phrase(String.format("%.2f %%", porcScrapp), normalFont));


        PdfPCell cellTotalTitulo = new PdfPCell(new Phrase("TOTAL ", boldFont));
        cellTotalTitulo.setBackgroundColor(BaseColor.LIGHT_GRAY);
        tablaBalance.addCell(cellTotalTitulo);

        PdfPCell cellTotalKilos = new PdfPCell(new Phrase(String.format("%.2f Kg", totalBalanceKg), boldFont));
        cellTotalKilos.setBackgroundColor(BaseColor.LIGHT_GRAY);
        tablaBalance.addCell(cellTotalKilos);

        PdfPCell cellTotalPorc = new PdfPCell(new Phrase("100.00 %", boldFont));
        cellTotalPorc.setBackgroundColor(BaseColor.LIGHT_GRAY);
        tablaBalance.addCell(cellTotalPorc);


        document.add(tablaBalance);

        if(turno.getComentarios() != null){
            document.add(new Paragraph("Observaciones: " + turno.getComentarios(), normalFont));
        }

        if (turno.getBobinas() != null && !turno.getBobinas().isEmpty()) {
            for (BobinaEX bobina : turno.getBobinas()) {

                agregarPaginaBobina(writer, document, bobina, ot, usuarioResponsable);
            }
        }
        document.close();
        return out.toByteArray();
    }




    private void agregarPaginaBobina(PdfWriter writer, Document document, BobinaEX bobina, OrdenTrabajoEX ot, String operador) throws DocumentException {


        Font fontHeaderGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 11);


        String valCodigoProducto = (ot.getProducto() != null) ? ot.getProducto().getCode() : "SIN CÓDIGO";

        String valNombreProducto = (ot.getProducto() != null) ? ot.getProducto().getName() : "N/A";

        String valCodigoOT = ot.getCodigo();
        String valCodigoBobina = (bobina.getCodigo() != null) ? bobina.getCodigo() : "-";
        String valPesoBruto = String.valueOf(bobina.getPesoBruto()) + " Kg";
        String valPesoNeto = String.valueOf(bobina.getPesoNeto()) + " Kg";

        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String valFecha = LocalDate.now().format(formatterFecha);

        String valTurno = "2";
        LocalTime horaActual = LocalTime.now();
        LocalTime inicioTurno1 = LocalTime.of(7, 0);
        LocalTime inicioTurno2 = LocalTime.of(19, 0);
        if (!horaActual.isBefore(inicioTurno1) && horaActual.isBefore(inicioTurno2)) {
            valTurno = "1";
        } else {
            valTurno = "2";
        }


        String valMaquina = (ot.getMaquina() != null) ? ot.getMaquina().getCodigo() : "N/A";
        String valOperador = operador;


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setKeepTogether(true);
        table.setSpacingAfter(20f);


        table.setWidths(new float[]{1f, 1f, 1f, 1f});


        PdfPCell cellCodProd = new PdfPCell(new Phrase(valCodigoProducto, fontHeaderGrande));
        cellCodProd.setColspan(4);
        cellCodProd.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCodProd.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellCodProd.setPadding(8f);
        table.addCell(cellCodProd);


        PdfPCell cellNomProd = new PdfPCell(new Phrase(valNombreProducto, fontHeaderGrande));
        cellNomProd.setColspan(4);
        cellNomProd.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellNomProd.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellNomProd.setPadding(8f);
        table.addCell(cellNomProd);


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
        cellBarcode.setMinimumHeight(50f);
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

        cell.setPadding(5f);
        table.addCell(cell);
    }


    private void addCellValue(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setMinimumHeight(20f);
        table.addCell(cell);
    }


    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    @GetMapping("/historial/{otId}")
    public ResponseEntity<List<TurnoEX>> listarPorOT(@PathVariable Long otId) {
        return ResponseEntity.ok(turnoRepository.findByOrdenTrabajoIdOrderByIdDesc(otId));
    }
}
