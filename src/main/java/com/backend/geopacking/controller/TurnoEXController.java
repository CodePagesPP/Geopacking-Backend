package com.backend.geopacking.controller;

import com.backend.geopacking.dto.RegistroTurnoDTO;
import com.backend.geopacking.model.*;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

        // 1. Buscar la Orden de Trabajo
        OrdenTrabajoEX ot = otRepository.findById(dto.getOtId())
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));

        // 2. Crear el Turno
        TurnoEX turno = new TurnoEX();
        turno.setFechaHoraFin(LocalDateTime.now());
        turno.setOrdenTrabajo(ot);
        turno.setComentarios(dto.getComentarios());
        turno.setUsuarioNombre(dto.getUsuarioNombre());

        // 3. Procesar Bobinas y calcular Total Kilos
        double totalKilos = 0; // Variable local segura
        List<BobinaEX> bobinas = dto.getBobinas();
        if (bobinas != null) {
            for (BobinaEX b : bobinas) {
                b.setTurno(turno);
                totalKilos += b.getPesoNeto();
            }
        }
        turno.setBobinas(bobinas);
        turno.setCantidadBobinas(bobinas != null ? bobinas.size() : 0);
        turno.setTotalKilosProducidos(totalKilos); // Guardamos en el objeto

        // 4. Procesar Materiales
        List<MaterialEX> materiales = dto.getMateriales();
        if (materiales != null) {
            for (MaterialEX m : materiales) {
                m.setTurno(turno);
            }
        }
        turno.setMateriales(materiales);

        // 5. Procesar Scrapp (Usando la lógica unidireccional que definimos)
        List<ScrappEX> listaScrappParaGuardar = new ArrayList<>();

        if (dto.getScrapp() != null) {
            for (RegistroTurnoDTO.ScrappDTO sDto : dto.getScrapp()) {
                ScrappEX s = new ScrappEX();
                s.setTipo(sDto.getTipo());         // Guardamos el nombre (Ej: S1)
                s.setCantidad(sDto.getCantidad()); // Guardamos los Kg
                s.setTurno(turno);                 // Vinculamos al turno

                listaScrappParaGuardar.add(s);
            }
        }
        turno.setScrapps(listaScrappParaGuardar);

        // 6. ACTUALIZAR ESTADO Y ACUMULADOS (Aquí estaba el error)
        // Usamos 'totalKilos' (local) en vez de turno.getTotalKilosProducidos() para evitar nulos
        double producidoAnterior = ot.getProducidoKg() == null ? 0 : ot.getProducidoKg();
        double acumuladoActual = producidoAnterior + totalKilos;

        ot.setProducidoKg(acumuladoActual);

        // Lógica de cambio de estado
        if (acumuladoActual >= ot.getRequerimientoKg()) {
            ot.setEstado(EstadoOT_EX.COMPLETADO);
        } else {
            ot.setEstado(EstadoOT_EX.EN_PROCESO);
        }

        // 7. Guardar en Base de Datos
        otRepository.save(ot);
        TurnoEX turnoGuardado = turnoRepository.save(turno);

        // 8. Generar PDF
        try {
            byte[] pdfBytes = generarPdfInterno(turnoGuardado, ot, dto.getUsuarioNombre());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Resumen_" + ot.getCodigo() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (DocumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private byte[] generarPdfInterno(TurnoEX turno, OrdenTrabajoEX ot, String usuarioResponsable) throws DocumentException {
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

            // Encabezados
            addHeaderCell(tablaScrapp, "Tipo");
            addHeaderCell(tablaScrapp, "Cantidad (Kg)");

            // Datos
            for (ScrappEX s : turno.getScrapps()) {
                tablaScrapp.addCell(new Phrase(s.getTipo(), normalFont));
                tablaScrapp.addCell(new Phrase(String.valueOf(s.getCantidad()), normalFont));
            }
            document.add(tablaScrapp);
        }





        document.add(new Paragraph(" ")); // Espacio separador

        // --- CÁLCULOS PARA LA TABLA DE BALANCE ---
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

        // --- CREACIÓN DE LA TABLA PDF ---
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
