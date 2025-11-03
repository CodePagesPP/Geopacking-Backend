package com.backend.geopacking.service.impl;

import com.backend.geopacking.model.Maquina;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.ScrappRepository;
import com.backend.geopacking.service.EtiquetaService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EtiquetaServiceImpl implements EtiquetaService {
    private final ScrappRepository registroScrappRepository;

    @Override
    public byte[] generarEtiquetaScrapp(Long registroId) throws Exception {
        Scrapp registro = registroScrappRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro de Scrapp no encontrado"));

        User user = registro.getOperador();
        Maquina maquina = registro.getMaquina();

        String operador = user.getName() + " " + user.getLastName();
        String codigoMaquina = maquina.getCodigo();
        String fecha = registro.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String turno = registro.getTurno();
        String contenido = "MOLIDO POLIPROPILENO NATURAL";

        // ¡¡AQUÍ ESTÁ LA LÓGICA DINÁMICA!!
        String anioCorto = String.valueOf(registro.getAnio()).substring(2);
        String bolson = registro.getNumeroBolson() + "/" + anioCorto;

        double pesoBruto = registro.getPesoBruto();
        double pesoNeto = registro.getPesoNeto();
        // --- 2. CREACIÓN DEL DOCUMENTO (Tamaño sin cambios, márgenes pequeños) ---
        float ancho = 160f * 2.83465f; // 160mm a puntos
        float alto = 100f * 2.83465f;  // 100mm a puntos
        Document document = new Document(new Rectangle(ancho, alto));
        document.setMargins(10, 10, 10, 10); // Márgenes pequeños

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // --- 3. FUENTES ---
        Font fontTitle = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font fontLabel = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font fontValue = new Font(Font.FontFamily.HELVETICA, 10);
        Font fontContent = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font fontSmall = new Font(Font.FontFamily.HELVETICA, 9);
        Font fontSmallBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        // --- 4. RECUADRO PRINCIPAL (LA CLAVE) ---
        // Crearemos una tabla principal que actuará como el contenedor
        // y su *única celda* tendrá el borde negro.
        PdfPTable mainContainerTable = new PdfPTable(1);
        mainContainerTable.setWidthPercentage(100);

        PdfPCell mainCell = new PdfPCell();
        mainCell.setBorder(Rectangle.BOX); // ¡El borde principal!
        mainCell.setBorderWidth(1.5f);
        mainCell.setPadding(5f);


        // --- 5. TÍTULO (DENTRO DE mainCell) ---
        Paragraph titulo = new Paragraph("MATERIAL PROCESADO EN MOLINO", fontTitle);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(3f);
        mainCell.addElement(titulo);


        // --- 6. HEADER (Contenido y Aprobación, DENTRO DE mainCell) ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60f, 40f}); // 60% para contenido, 40% para aprobación
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Celda Izquierda (Contenido)
        PdfPCell contentTitleCell = new PdfPCell();
        contentTitleCell.setBorder(Rectangle.NO_BORDER);
        contentTitleCell.setVerticalAlignment(Element.ALIGN_TOP);
        contentTitleCell.setPaddingRight(10f);

        Paragraph pContenidoLabel = new Paragraph("CONTENIDO:", fontLabel);
        contentTitleCell.addElement(pContenidoLabel);

        Paragraph pContenidoValue = new Paragraph(contenido, fontContent);
        pContenidoValue.setLeading(14f); // Espacio entre líneas
        contentTitleCell.addElement(pContenidoValue);

        headerTable.addCell(contentTitleCell);

        // Celda Derecha (Aprobación)
        PdfPCell approvalCell = new PdfPCell();
        approvalCell.setBorder(Rectangle.NO_BORDER);
        approvalCell.setVerticalAlignment(Element.ALIGN_TOP);
        approvalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // La tabla de aprobación anidada
        approvalCell.addElement(createApprovalTable(fontSmall, fontSmallBold));
        headerTable.addCell(approvalCell);

        mainCell.addElement(headerTable);


        // --- 7. DATA BLOCK (DENTRO DE mainCell) ---
        // Esta es la tabla de 4 columnas (Label, Value, Label, Value)
        PdfPTable dataTable = new PdfPTable(4);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new float[]{30f, 20f, 30f, 20f});
        dataTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        dataTable.setSpacingBefore(8f); // Espacio después del bloque de contenido
        agregarFilaDatos(dataTable, "BOLSON N°:", bolson, fontLabel, fontValue,
                "MAQUINA:", codigoMaquina, fontValue, fontValue);

        // Fila 2: PESO BRUTO y FECHA/TURNO
        agregarFilaDatos(dataTable, "PESO BRUTO (Kg.):", String.valueOf(pesoBruto), fontValue, fontValue,
                "FECHA Y TURNO:", fecha + " - " + turno, fontValue, fontValue);

        // Fila 3: PESO NETO y OPERADOR
        agregarFilaDatos(dataTable, "PESO NETO (Kg.):", String.valueOf(pesoNeto), fontLabel, fontValue,
                "OPERADOR:", operador, fontValue, fontValue);

        mainCell.addElement(dataTable);
        Paragraph pObs = new Paragraph("Observaciones:", fontValue);
        pObs.setSpacingBefore(5f);
        mainCell.addElement(pObs);

        PdfPCell obsBoxCell = new PdfPCell(new Phrase(" ")); // Celda vacía para el recuadro
        obsBoxCell.setBorder(Rectangle.BOX);
        obsBoxCell.setBorderWidth(0.5f);
        obsBoxCell.setFixedHeight(20f);

        PdfPTable obsTable = new PdfPTable(1);
        obsTable.setWidthPercentage(100);
        obsTable.addCell(obsBoxCell);

        mainCell.addElement(obsTable);


        // --- 9. AÑADIR LA CELDA PRINCIPAL AL DOCUMENTO ---
        mainContainerTable.addCell(mainCell);
        document.add(mainContainerTable);


        // --- 10. CÓDIGO DE BARRAS (FUERA DEL RECUADRO) ---
        document.add(new Paragraph("\n")); // Espacio entre el recuadro y el código de barras
        PdfContentByte cb = writer.getDirectContent();
        String barcodeText = "MOLPP-" + bolson.replace("/", "-") + "-" + String.valueOf((int)pesoNeto);

        Barcode128 barcode = new Barcode128();
        barcode.setCode(barcodeText.trim());
        barcode.setCodeType(Barcode128.CODE_A);
        barcode.setBarHeight(30f);
        barcode.setX(0.75f);

        Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
        barcodeImage.setAlignment(Element.ALIGN_CENTER);
        document.add(barcodeImage);

        Paragraph barcodeLabel = new Paragraph("*" + barcodeText + "*", fontValue);
        barcodeLabel.setAlignment(Element.ALIGN_CENTER);
        document.add(barcodeLabel);
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
