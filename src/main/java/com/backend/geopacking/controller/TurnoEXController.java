package com.backend.geopacking.controller;

import com.backend.geopacking.dto.RegistroTurnoDTO;
import com.backend.geopacking.model.BobinaEX;
import com.backend.geopacking.model.MaterialEX;
import com.backend.geopacking.model.OrdenTrabajoEX;
import com.backend.geopacking.model.TurnoEX;
import com.backend.geopacking.repository.OrdenTrabajoEXRepository;
import com.backend.geopacking.repository.TurnoEXRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/turno-ex")
public class TurnoEXController {

    @Autowired
    private OrdenTrabajoEXRepository otRepository;
    @Autowired
    private TurnoEXRepository turnoRepository;

    @PostMapping("/finalizar")
    @Transactional
    public ResponseEntity<byte[]> finalizarTurno(@RequestBody RegistroTurnoDTO dto) {


        OrdenTrabajoEX ot = otRepository.findById(dto.getOtId())
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));

        TurnoEX turno = new TurnoEX();
        turno.setFechaHoraFin(LocalDateTime.now());
        turno.setOrdenTrabajo(ot);

        double totalKilos = 0;
        List<BobinaEX> bobinas = dto.getBobinas();
        if (bobinas != null) {
            for (BobinaEX b : bobinas) {
                b.setTurno(turno);
                totalKilos += b.getPesoNeto();
            }
        }

        List<MaterialEX> materiales = dto.getMateriales();
        if (materiales != null) {
            for (MaterialEX m : materiales) {
                m.setTurno(turno);
            }
        }

        turno.setBobinas(bobinas);
        turno.setMateriales(materiales);
        turno.setTotalKilosProducidos(totalKilos);
        turno.setCantidadBobinas(bobinas != null ? bobinas.size() : 0);

        double acumuladoActual = ot.getProducidoKg() == null ? 0 : ot.getProducidoKg();
        ot.setProducidoKg(acumuladoActual + totalKilos);

        otRepository.save(ot);
        TurnoEX turnoGuardado = turnoRepository.save(turno);


        try {
            byte[] pdfBytes = generarPdfInterno(turnoGuardado, ot);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Resumen_OT" + ot.getCodigo() + "_" + System.currentTimeMillis() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (DocumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private byte[] generarPdfInterno(TurnoEX turno, OrdenTrabajoEX ot) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

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



        String infoProducto = "N/A";
        if (ot.getProducto() != null) {

            infoProducto = ot.getProducto().getName();
        }
        document.add(new Paragraph("Producto: " + infoProducto, normalFont));
        document.add(new Paragraph("Fecha Fin: " + turno.getFechaHoraFin(), normalFont));
        document.add(new Paragraph(" "));


        if (turno.getBobinas() != null && !turno.getBobinas().isEmpty()) {
            document.add(new Paragraph("Bobinas Producidas:", boldFont));
            document.add(new Paragraph(" "));

            PdfPTable tablaBobinas = new PdfPTable(3);
            tablaBobinas.setWidthPercentage(100);


            addHeaderCell(tablaBobinas, "Código Bobina");
            addHeaderCell(tablaBobinas, "Peso Bruto");
            addHeaderCell(tablaBobinas, "Peso Neto");


            for (BobinaEX b : turno.getBobinas()) {
                tablaBobinas.addCell(new Phrase(b.getCodigo() != null ? b.getCodigo() : "-", normalFont));
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

        document.close();
        return out.toByteArray();
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
