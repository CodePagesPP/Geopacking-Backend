package com.backend.geopacking.service.impl;

import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.ScrappRepository;
import com.backend.geopacking.service.EtiquetaService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtiquetaServiceImpl implements EtiquetaService {
    private final ScrappRepository registroScrappRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generarEtiquetaScrapp(Long registroId) throws Exception {
        Scrapp registro = registroScrappRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro de Scrapp no encontrado"));

        // --- Lógica de Datos (sin cambios) ---
        User user = registro.getOperador();
        Maquina maquina = registro.getMaquina();
        String operador = user.getName() + " " + user.getLastName();
        String codigoMaquina = maquina.getCodigo();
        String fecha = registro.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String turno = registro.getTurno();
        String contenido = "MOLIDO POLIPROPILENO NATURAL";
        String anioCorto = String.valueOf(registro.getAnio()).substring(2);
        String bolson = registro.getNumeroBolson() + "/" + anioCorto;
        double pesoBruto = registro.getPesoBruto();
        double pesoNeto = registro.getPesoNeto();
        String origenesStr = "N/A";
        if (maquina instanceof Molino) {
            Molino molino = (Molino) maquina;
            Set<Origen> origenes = molino.getOrigenes();
            if (origenes != null && !origenes.isEmpty()) {
                origenesStr = origenes.stream()
                        .map(Origen::getCode)
                        .collect(Collectors.joining(", "));
            }
        }
        // --- Fin Lógica de Datos ---

        // --- 1. DEFINICIÓN DEL DOCUMENTO (Sin cambios) ---
        float ancho = 160f * 2.83465f; // 160mm
        float alto = 100f * 2.83465f;  // 100mm (Mantenemos el alto original)
        Document document = new Document(new Rectangle(ancho, alto));
        document.setMargins(5, 5, 5, 5); // Márgenes pequeños

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // --- 2. FUENTES (Sin cambios) ---
        Font fontTitle = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font fontLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        Font fontValue = new Font(Font.FontFamily.HELVETICA, 9);
        Font fontContent = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font fontSmall = new Font(Font.FontFamily.HELVETICA, 8);
        Font fontSmallBold = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);

        // <-- ¡NUEVA FUENTE! para el texto legible del barcode
        Font fontBarcodeText = new Font(Font.FontFamily.HELVETICA, 8);

        // --- 3. ESTRUCTURA DE TABLA PRINCIPAL (Sin cambios) ---
        PdfPTable mainContainerTable = new PdfPTable(1);
        mainContainerTable.setWidthPercentage(100);

        // --- CELDA 1: El RECUADRO (Sin cambios) ---
        PdfPCell mainCell = new PdfPCell();
        mainCell.setBorder(Rectangle.BOX);
        mainCell.setBorderWidth(1.5f);
        mainCell.setPadding(3f);
        mainCell.setPaddingBottom(5f);

        // --- TÍTULO (Dentro de mainCell) (Sin cambios) ---
        Paragraph titulo = new Paragraph("MATERIAL PROCESADO EN MOLINO", fontTitle);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(2f);
        mainCell.addElement(titulo);

        // --- HEADER (Dentro de mainCell) (Sin cambios) ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60f, 40f});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        // (Celda Izquierda)
        PdfPCell contentTitleCell = new PdfPCell();
        contentTitleCell.setBorder(Rectangle.NO_BORDER);
        contentTitleCell.setVerticalAlignment(Element.ALIGN_TOP);
        contentTitleCell.setPaddingRight(10f);
        Paragraph pContenidoLabel = new Paragraph("CONTENIDO:", fontLabel);
        contentTitleCell.addElement(pContenidoLabel);
        Paragraph pContenidoValue = new Paragraph(contenido, fontContent);
        pContenidoValue.setLeading(12f);
        contentTitleCell.addElement(pContenidoValue);
        headerTable.addCell(contentTitleCell);
        // (Celda Derecha)
        PdfPCell approvalCell = new PdfPCell();
        approvalCell.setBorder(Rectangle.NO_BORDER);
        approvalCell.setVerticalAlignment(Element.ALIGN_TOP);
        approvalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        approvalCell.addElement(createApprovalTable(fontSmall, fontSmallBold));
        headerTable.addCell(approvalCell);
        mainCell.addElement(headerTable);

        // --- DATA BLOCK (Dentro de mainCell) (Sin cambios) ---
        PdfPTable dataTable = new PdfPTable(4);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new float[]{30f, 20f, 30f, 20f});
        dataTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        dataTable.setSpacingBefore(4f);
        // (Fila 1)
        agregarFilaDatos(dataTable, "BOLSON N°:", bolson, fontLabel, fontValue,
                "MAQUINA:", codigoMaquina, fontValue, fontValue);
        // (Fila 2)
        PdfPCell origenLabelCell = new PdfPCell(new Paragraph("ORIGEN:", fontLabel));
        origenLabelCell.setBorder(Rectangle.NO_BORDER);
        origenLabelCell.setPaddingTop(1f);
        origenLabelCell.setPaddingBottom(1f);
        dataTable.addCell(origenLabelCell);
        PdfPCell origenValueCell = new PdfPCell(new Paragraph(origenesStr, fontValue));
        origenValueCell.setBorder(Rectangle.NO_BORDER);
        origenValueCell.setColspan(3);
        origenValueCell.setPaddingTop(1f);
        origenValueCell.setPaddingBottom(1f);
        dataTable.addCell(origenValueCell);
        // (Fila 3)
        agregarFilaDatos(dataTable, "PESO BRUTO (Kg.):", String.valueOf(pesoBruto), fontValue, fontValue,
                "FECHA Y TURNO:", fecha + " - " + turno, fontValue, fontValue);
        // (Fila 4)
        agregarFilaDatos(dataTable, "PESO NETO (Kg.):", String.valueOf(pesoNeto), fontLabel, fontValue,
                "OPERADOR:", operador, fontValue, fontValue);
        mainCell.addElement(dataTable);

        // --- OBSERVACIONES (Dentro de mainCell) (Sin cambios) ---
        Paragraph pObs = new Paragraph("Observaciones:", fontValue);
        pObs.setSpacingBefore(3f);
        mainCell.addElement(pObs);
        PdfPCell obsBoxCell = new PdfPCell(new Phrase(" "));
        obsBoxCell.setBorder(Rectangle.BOX);
        obsBoxCell.setBorderWidth(0.5f);
        obsBoxCell.setFixedHeight(15f);
        PdfPTable obsTable = new PdfPTable(1);
        obsTable.setWidthPercentage(100);
        obsTable.addCell(obsBoxCell);
        mainCell.addElement(obsTable);


        mainContainerTable.addCell(mainCell);


        PdfContentByte cb = writer.getDirectContent();
        String barcodeText = "MOLPP-" + bolson.replace("/", "-") + "-" + String.valueOf((int)pesoNeto);

        Barcode128 barcode = new Barcode128();
        barcode.setCode(barcodeText.trim());
        barcode.setCodeType(Barcode128.CODE_A);


        barcode.setBarHeight(40f);
        barcode.setX(0.75f);



        Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
        barcodeImage.setAlignment(Element.ALIGN_CENTER);

        barcodeImage.scalePercent(90f);


        PdfPCell barcodeCell = new PdfPCell();
        barcodeCell.setBorder(Rectangle.NO_BORDER);
        barcodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeCell.setPaddingTop(20f);

        barcodeCell.addElement(barcodeImage);


        mainContainerTable.addCell(barcodeCell);


        document.add(mainContainerTable);

        document.close();
        return baos.toByteArray();
    }

    private PdfPTable createApprovalTable(Font fontSmall, Font fontSmallBold) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(95); // Un poco más pequeño que su contenedor
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setPadding(1f);

        // Título con borde
        PdfPCell titleCell = new PdfPCell(new Phrase("Estado de Aprobación del Producto", fontSmallBold));
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setBorderWidth(0.5f);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(2f);
        table.addCell(titleCell);

        // Subtítulo
        PdfPCell subtitleCell = new PdfPCell(new Phrase("Marcar Con un Tilde Lo que Corresponda", fontSmall));
        subtitleCell.setBorder(Rectangle.NO_BORDER);
        subtitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(subtitleCell);

        // Opciones (usamos una tabla anidada de 2 columnas para "CONFORME [ ]")
        PdfPTable optionsTable = new PdfPTable(2);
        optionsTable.setWidthPercentage(100);
        optionsTable.setWidths(new float[]{70f, 30f});

        optionsTable.addCell(createOptionCell("CONFORME", fontSmall));
        optionsTable.addCell(createBoxCell(fontSmall));
        optionsTable.addCell(createOptionCell("NO CONFORME", fontSmall));
        optionsTable.addCell(createBoxCell(fontSmall));

        // Añadir la tabla de opciones a la tabla principal de aprobación
        PdfPCell optionsContainerCell = new PdfPCell(optionsTable);
        optionsContainerCell.setBorder(Rectangle.BOX); // Borde alrededor de las opciones
        optionsContainerCell.setBorderWidth(0.5f);
        table.addCell(optionsContainerCell);

        return table;
    }

    // Helper para la celda de opción (CONFORME, NO CONFORME)
    private PdfPCell createOptionCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    // Helper para la celda del cuadradito [ ]
    private PdfPCell createBoxCell(Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("[  ]", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }


    // MODIFICADO: Helper para la tabla de datos de 4 columnas
    private void agregarFilaDatos(PdfPTable tabla,
                                  String label1, String value1, Font label1Font, Font value1Font,
                                  String label2, String value2, Font label2Font, Font value2Font) {

        // Par 1 (Label + Value)
        PdfPCell c1 = new PdfPCell(new Phrase(label1, label1Font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPaddingBottom(3);
        tabla.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value1, value1Font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPaddingBottom(3);
        tabla.addCell(c2);

        // Par 2 (Label + Value)
        PdfPCell c3 = new PdfPCell(new Phrase(label2, label2Font));
        c3.setBorder(Rectangle.NO_BORDER);
        c3.setPaddingBottom(3);
        tabla.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase(value2, value2Font));
        c4.setBorder(Rectangle.NO_BORDER);
        c4.setPaddingBottom(3);
        tabla.addCell(c4);
    }

    // Tu método (sin cambios)
    private String obtenerTurnoActual() {
        int hora = LocalTime.now().getHour();
        if (hora < 14) return "M";
        else if (hora < 22) return "T";
        else return "N";
    }
}
