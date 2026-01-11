package com.backend.geopacking.service;

import com.backend.geopacking.model.DetalleProduccionTF;
import com.backend.geopacking.model.OrdenTrabajoTF;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfTfService {
    public byte[] generarEtiquetasPdf(OrdenTrabajoTF ot, DetalleProduccionTF detalle, int inicioSecuencia) throws DocumentException {
        Rectangle pageSize = new Rectangle(100f * 2.83465f, 50f * 2.83465f);
        Document document = new Document(pageSize);
        document.setMargins(5, 5, 5, 5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Fuentes
        Font fontProducto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7);

        String nombreProducto = ot.getProducto().getName();
        String codigoProducto = (ot.getProducto().getCode() != null) ? ot.getProducto().getCode() : "";
        String valorEmpaque = (ot.getProducto().getLinea() != null && !ot.getProducto().getLinea().isEmpty())
                ? ot.getProducto().getLinea()
                : "-";

        int cantidadCajas = detalle.getCajas();

        for (int i = 0; i < cantidadCajas; i++) {
            if (i > 0) document.newPage(); // Nueva etiqueta por cada caja

            int numeroCajaActual = inicioSecuencia + i;

            // CONTENEDOR PRINCIPAL
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setKeepTogether(true);
            mainTable.setWidthPercentage(100);
            mainTable.getDefaultCell().setBorder(Rectangle.BOX);
            mainTable.getDefaultCell().setBorderWidth(1.5f);
            mainTable.getDefaultCell().setPadding(0);

            // 1. NOMBRE PRODUCTO (Arriba, centrado)
            PdfPCell cellHeader = new PdfPCell();
            cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellHeader.setBorder(Rectangle.BOTTOM);
            cellHeader.setPaddingTop(2f);
            cellHeader.setPaddingBottom(5f);
            cellHeader.setFixedHeight(35f);

            Paragraph pCodigo = new Paragraph(codigoProducto, fontLabel);
            pCodigo.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(pCodigo);

            Paragraph pNombre = new Paragraph(nombreProducto, fontProducto);
            pNombre.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(pNombre);

            mainTable.addCell(cellHeader);

            // 2. TABLA INTERMEDIA (Empaque | Lote | # Caja)
            PdfPTable midTable = new PdfPTable(3);
            midTable.setWidthPercentage(100);
            midTable.setWidths(new float[]{1.2f, 1f, 0.8f});

            midTable.addCell(crearCeldaDoble("EMPAQUE", valorEmpaque, fontLabel, fontValue));
            midTable.addCell(crearCeldaDoble("COD LOTE", detalle.getLoteBobina(), fontLabel, fontValue));
            midTable.addCell(crearCeldaDoble("# CAJA", String.valueOf(numeroCajaActual), fontLabel, fontValue));

            PdfPCell cellMidContainer = new PdfPCell(midTable);
            cellMidContainer.setBorder(Rectangle.BOTTOM);
            cellMidContainer.setPadding(0);
            mainTable.addCell(cellMidContainer);

            // 3. CÓDIGO DE BARRAS
            PdfPCell cellBarcode = new PdfPCell();
            cellBarcode.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBarcode.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellBarcode.setPadding(5f);
            cellBarcode.setBorder(Rectangle.NO_BORDER);

            String codigoTexto = ot.getCodigo() + "-" + numeroCajaActual;

            try {
                PdfContentByte cb = writer.getDirectContent();
                Barcode128 code128 = new Barcode128();
                code128.setCode(codigoTexto);
                code128.setCodeType(Barcode128.CODE128);
                code128.setBarHeight(12f);
                code128.setFont(null); // Sin texto automático

                Image image128 = code128.createImageWithBarcode(cb, null, null);
                cellBarcode.addElement(image128);

                // Texto legible manual debajo
                Paragraph pCode = new Paragraph(codigoTexto, fontSmall);
                pCode.setAlignment(Element.ALIGN_CENTER);
                cellBarcode.addElement(pCode);

            } catch (Exception e) {
                cellBarcode.addElement(new Phrase("ERROR BARCODE"));
            }

            mainTable.addCell(cellBarcode);

            // Agregar al doc
            document.add(mainTable);
        }

        document.close();
        return baos.toByteArray();
    }

    // --- 2. GENERAR REPORTE DE AVANCE (Lo que ves en pantalla en PDF) ---
    public byte[] generarReporteAvance(OrdenTrabajoTF ot, List<DetalleProduccionTF> detalles, String observaciones, String nombreOperador) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // --- FUENTES ---
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontHeaderBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontHeaderNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        // Fuente blanca para el encabezado de la tabla (Estilo Extrusión)
        Font fontTablaHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        Font fontRow = FontFactory.getFont(FontFactory.HELVETICA, 9);

        // --- 1. TÍTULO ---
        Paragraph titulo = new Paragraph("REPORTE DE FIN DE TURNO - TERMOFORMADO", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(15f);
        document.add(titulo);

        // --- 2. CÁLCULO DE FECHA Y TURNO ---
        LocalDateTime ahora = LocalDateTime.now();
        String fechaActual = ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Lógica de turno: 7am a 7pm = Turno 1, sino Turno 2
        int hora = ahora.getHour();
        String turnoActual = (hora >= 7 && hora < 19) ? "1" : "2";

        // --- 3. DATOS GENERALES (BLOQUE 1) ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.2f, 2f});
        headerTable.setSpacingAfter(5f);

        agregarFilaHeader(headerTable, "Código OT:", ot.getCodigo(), fontHeaderBold, fontHeaderNormal);
        agregarFilaHeader(headerTable, "Máquina:", ot.getMaquina().getCodigo(), fontHeaderBold, fontHeaderNormal);
        // Aquí usamos el nombre real que pasaste desde el controller
        agregarFilaHeader(headerTable, "Operador Responsable:", nombreOperador, fontHeaderBold, fontHeaderNormal);

        document.add(headerTable);

        // --- LÍNEA SEPARADORA ---
        LineSeparator separator = new LineSeparator();
        separator.setPercentage(100);
        separator.setLineColor(BaseColor.GRAY);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" ")); // Pequeño salto

        // --- 4. DATOS GENERALES (BLOQUE 2) ---
        PdfPTable headerTable2 = new PdfPTable(2);
        headerTable2.setWidthPercentage(100);
        headerTable2.setWidths(new float[]{1.2f, 2f});
        headerTable2.setSpacingAfter(15f);

        agregarFilaHeader(headerTable2, "Producto:", ot.getProducto().getName(), fontHeaderBold, fontHeaderNormal);
        agregarFilaHeader(headerTable2, "Fecha Reporte:", fechaActual, fontHeaderBold, fontHeaderNormal);
        agregarFilaHeader(headerTable2, "Turno:", turnoActual, fontHeaderBold, fontHeaderNormal);

        document.add(headerTable2);

        // --- 5. TABLA DE PRODUCCIÓN ---
        Paragraph subtitulo = new Paragraph("Detalle de Producción:", fontHeaderBold);
        subtitulo.setSpacingAfter(5f);
        document.add(subtitulo);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1.8f, 1f, 1.2f, 1.2f, 1f, 1.2f});

        // Encabezados con Fondo Gris Oscuro (Look Profesional)
        BaseColor grisHeader = new BaseColor(80, 80, 80);
        addCellHeaderColor(table, "Bobina", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "Lote", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "Veloc.", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "H. Inicio", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "H. Fin", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "Cajas", fontTablaHeader, grisHeader);
        addCellHeaderColor(table, "Rechazo", fontTablaHeader, grisHeader);

        int totalCajas = 0;
        double totalRechazo = 0;

        for (DetalleProduccionTF det : detalles) {
            addCellCentered(table, det.getCodigoBobina(), fontRow);
            addCellCentered(table, det.getLoteBobina(), fontRow);

            String velStr = (det.getVelocidad() != null) ? String.valueOf(det.getVelocidad()) : "-";
            addCellCentered(table, velStr, fontRow);

            addCellCentered(table, det.getHoraInicio() != null ? det.getHoraInicio().toString() : "-", fontRow);
            addCellCentered(table, det.getHoraFin() != null ? det.getHoraFin().toString() : "-", fontRow);

            addCellCentered(table, String.valueOf(det.getCajas()), fontRow);
            addCellCentered(table, String.format("%.2f", det.getRechazoKg()), fontRow);

            totalCajas += det.getCajas();
            totalRechazo += det.getRechazoKg();
        }

        document.add(table);

        // --- 6. TOTALES ---
        document.add(new Paragraph(" "));
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // Fila Total Cajas
        PdfPCell cellTotalLabel = new PdfPCell(new Phrase("Total Cajas:", fontHeaderBold));
        cellTotalLabel.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(cellTotalLabel);

        PdfPCell cellTotalValue = new PdfPCell(new Phrase(String.valueOf(totalCajas), fontRow));
        cellTotalValue.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(cellTotalValue);

        // Fila Total Rechazo
        PdfPCell cellRechazoLabel = new PdfPCell(new Phrase("Total Rechazo (Kg):", fontHeaderBold));
        cellRechazoLabel.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(cellRechazoLabel);

        PdfPCell cellRechazoValue = new PdfPCell(new Phrase(String.format("%.2f", totalRechazo), fontRow));
        cellRechazoValue.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(cellRechazoValue);

        document.add(totalTable);


        // --- 7. OBSERVACIONES ---
        if (observaciones != null && !observaciones.isEmpty()) {
            document.add(new Paragraph(" "));
            document.add(new Paragraph("OBSERVACIONES GENERALES:", fontHeaderBold));

            PdfPTable obsTable = new PdfPTable(1);
            obsTable.setWidthPercentage(100);
            PdfPCell cellObs = new PdfPCell(new Phrase(observaciones, fontRow));
            cellObs.setPadding(8f);
            cellObs.setBackgroundColor(new BaseColor(240, 240, 240)); // Gris muy clarito de fondo
            cellObs.setBorderColor(BaseColor.LIGHT_GRAY);
            obsTable.addCell(cellObs);

            document.add(obsTable);
        }

        document.close();
        return baos.toByteArray();
    }

    // Helper para celdas de etiqueta
    private PdfPCell crearCeldaDoble(String titulo, String valor, Font fTitulo, Font fValor) {
        PdfPCell cell = new PdfPCell();
        // Bordes: Solo derecha para separar columnas (el último no necesita)
        cell.setBorder(Rectangle.RIGHT);

        Paragraph pTitulo = new Paragraph(titulo, fTitulo);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pTitulo);

        Paragraph pValor = new Paragraph(valor, fValor);
        pValor.setAlignment(Element.ALIGN_CENTER);
        pValor.setSpacingBefore(2f);
        cell.addElement(pValor);

        cell.setPadding(4f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarFilaHeader(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPaddingBottom(3f);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value != null ? value : "-", fontValue));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPaddingBottom(3f);
        table.addCell(cellValue);
    }

    private void addCellHeaderColor(PdfPTable table, String text, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(color);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addCellCentered(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
