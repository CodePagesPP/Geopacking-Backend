package com.backend.geopacking.service;

import com.backend.geopacking.model.DetalleProduccionTF;
import com.backend.geopacking.model.OrdenTrabajoTF;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfTfService {
    public byte[] generarEtiquetasPdf(OrdenTrabajoTF ot, DetalleProduccionTF detalle, int inicioSecuencia) throws DocumentException {
        // Tamaño etiqueta: 100mm x 50mm (Ajustable a tu impresora Zebra/Térmica)
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

        int cantidadCajas = detalle.getCajas();

        for (int i = 0; i < cantidadCajas; i++) {
            if (i > 0) document.newPage(); // Nueva etiqueta por cada caja

            int numeroCajaActual = inicioSecuencia + i;

            // --- CONTENEDOR PRINCIPAL CON BORDE ---
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setKeepTogether(true);
            mainTable.setWidthPercentage(100);
            mainTable.getDefaultCell().setBorder(Rectangle.BOX);
            mainTable.getDefaultCell().setBorderWidth(1.5f);
            mainTable.getDefaultCell().setPadding(0);

            // 1. NOMBRE PRODUCTO (Arriba, centrado)
            PdfPCell cellProducto = new PdfPCell(new Phrase(ot.getProducto().getName(), fontProducto));
            cellProducto.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellProducto.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellProducto.setPadding(8f);
            cellProducto.setBorder(Rectangle.BOTTOM);
            cellProducto.setFixedHeight(30f);
            mainTable.addCell(cellProducto);

            // 2. TABLA INTERMEDIA (Empaque | Lote | # Caja)
            PdfPTable midTable = new PdfPTable(3);
            midTable.setWidthPercentage(100);
            midTable.setWidths(new float[]{1f, 1f, 1f}); // 3 columnas iguales

            // Agregamos las 3 celdas
            midTable.addCell(crearCeldaDoble("EMPAQUE", "Caja Standard", fontLabel, fontValue));
            midTable.addCell(crearCeldaDoble("COD LOTE", detalle.getLoteBobina(), fontLabel, fontValue));
            midTable.addCell(crearCeldaDoble("# CAJA", String.valueOf(numeroCajaActual), fontLabel, fontValue));

            PdfPCell cellMidContainer = new PdfPCell(midTable);
            cellMidContainer.setBorder(Rectangle.BOTTOM);
            cellMidContainer.setPadding(0);
            mainTable.addCell(cellMidContainer);

            // 3. CÓDIGO DE BARRAS (Abajo)
            PdfPCell cellBarcode = new PdfPCell();
            cellBarcode.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBarcode.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellBarcode.setPadding(5f);
            cellBarcode.setBorder(Rectangle.NO_BORDER);

            // Generar Código: OT + Caja (Ej: OT-001-150)
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
    public byte[] generarReporteAvance(OrdenTrabajoTF ot, List<DetalleProduccionTF> detalles, String observaciones) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontRow = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Título
        Paragraph titulo = new Paragraph("AVANCE DE PRODUCCIÓN - TERMOFORMADO", fontTitle);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));

        // Info OT
        document.add(new Paragraph("Orden: " + ot.getCodigo(), fontRow));
        document.add(new Paragraph("Producto: " + ot.getProducto().getName(), fontRow));
        document.add(new Paragraph("Máquina: " + ot.getMaquina().getCodigo(), fontRow));
        document.add(new Paragraph(" "));

        // Tabla
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 2f, 1.5f, 2f, 2f, 1.5f, 1.5f});

        addHeaderCell(table, "Bobina", fontHeader);
        addHeaderCell(table, "Lote", fontHeader);
        addHeaderCell(table, "Velocidad", fontHeader);
        addHeaderCell(table, "H. Inicio", fontHeader);
        addHeaderCell(table, "H. Fin", fontHeader);
        addHeaderCell(table, "Cajas", fontHeader);
        addHeaderCell(table, "Rechazo", fontHeader);

        int totalCajas = 0;
        double totalRechazo = 0;

        for (DetalleProduccionTF det : detalles) {
            table.addCell(new Phrase(det.getCodigoBobina(), fontRow));
            table.addCell(new Phrase(det.getLoteBobina(), fontRow));
            String velStr = (det.getVelocidad() != null) ? String.valueOf(det.getVelocidad()) : "-";
            table.addCell(new Phrase(velStr, fontRow));
            table.addCell(new Phrase(det.getHoraInicio() != null ? det.getHoraInicio().toString() : "-", fontRow));
            table.addCell(new Phrase(det.getHoraFin() != null ? det.getHoraFin().toString() : "-", fontRow));
            table.addCell(new Phrase(String.valueOf(det.getCajas()), fontRow));
            table.addCell(new Phrase(String.format("%.2f", det.getRechazoKg()), fontRow));

            totalCajas += det.getCajas();
            totalRechazo += det.getRechazoKg();
        }

        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Cajas Producidas: " + totalCajas, fontHeader));
        document.add(new Paragraph("Total Rechazo (Kg): " + String.format("%.2f", totalRechazo), fontHeader));

        if (observaciones != null && !observaciones.isEmpty()) {
            document.add(new Paragraph(" "));
            Font fontObsTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontObs = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("OBSERVACIONES GENERALES:", fontObsTitle));

            PdfPTable obsTable = new PdfPTable(1);
            obsTable.setWidthPercentage(100);
            PdfPCell cellObs = new PdfPCell(new Phrase(observaciones, fontObs));
            cellObs.setPadding(5f);
            cellObs.setBorder(Rectangle.BOX);
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
}
