package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.BobinaTransitoDTO;
import com.backend.geopacking.model.BobinaEX;
import com.backend.geopacking.repository.BobinaEXRepository;
import com.backend.geopacking.service.BobinaService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BobinaServiceImpl implements BobinaService {

    private final BobinaEXRepository bobinaRepository;
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    @Override
    @Transactional(readOnly = true)
    public List<BobinaTransitoDTO> listarBobinasEnTransito() {
        return bobinaRepository.findDisponiblesWithDetails().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BobinaTransitoDTO mapToDTO(BobinaEX b) {
        String prodNombre = "Desconocido";
        String otCodigo = "-";
        String operador = "Sin Asignar";
        LocalDateTime fecha = null;

        if (b.getTurno() != null) {
            fecha = b.getTurno().getFechaHoraFin();
            String nombreTurno = b.getTurno().getUsuarioNombre();

            if (nombreTurno != null && !nombreTurno.trim().equalsIgnoreCase("null null") && !nombreTurno.trim().isEmpty()) {
                operador = nombreTurno;
            } else {
                if (b.getTurno().getOrdenTrabajo() != null && b.getTurno().getOrdenTrabajo().getCreadaPor() != null) {
                    operador = b.getTurno().getOrdenTrabajo().getCreadaPor().getName();
                }
            }

            if (b.getTurno().getOrdenTrabajo() != null) {
                otCodigo = b.getTurno().getOrdenTrabajo().getCodigo();
                if (b.getTurno().getOrdenTrabajo().getProducto() != null) {
                    prodNombre = b.getTurno().getOrdenTrabajo().getProducto().getName();
                }
            }
        }

        return BobinaTransitoDTO.builder()
                .id(b.getId())
                .codigoBobina(b.getCodigo())
                .nombreProducto(prodNombre)
                .pesoBruto(b.getPesoBruto())
                .pesoNeto(b.getPesoNeto())
                .producidoPor(operador)
                .fecha(fecha)
                .codigoOT(otCodigo)
                .build();
    }


    @Override
    public byte[] generarReporteCompletoBobinasPdf(LocalDate fechaInicio, LocalDate fechaFin) {

        List<BobinaTransitoDTO> registros;
        double totalBruto = 0;
        double totalNeto = 0;
        String subtitulo;


        List<BobinaTransitoDTO> todos = listarBobinasEnTransito();

        if (fechaInicio != null && fechaFin != null) {

            registros = todos.stream()
                    .filter(b -> {
                        LocalDate fechaBobina = b.getFecha().toLocalDate();
                        return !fechaBobina.isBefore(fechaInicio) && !fechaBobina.isAfter(fechaFin);
                    })
                    .collect(Collectors.toList());

            subtitulo = "Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " al " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            registros = todos;
            subtitulo = "Todos los registros (Stock Actual)";
        }


        totalBruto = registros.stream().mapToDouble(b -> b.getPesoBruto() != null ? b.getPesoBruto() : 0).sum();
        totalNeto = registros.stream().mapToDouble(b -> b.getPesoNeto() != null ? b.getPesoNeto() : 0).sum();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30); // Márgenes

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Paragraph titulo = new Paragraph("Reporte Completo de Bobinas (Stock)", FONT_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph pSubtitulo = new Paragraph(subtitulo, FONT_SUBTITULO);
            pSubtitulo.setAlignment(Element.ALIGN_CENTER);
            pSubtitulo.setSpacingAfter(15f);
            document.add(pSubtitulo);

            float[] columnWidths = {0.7f, 1.5f, 2.5f, 1.5f, 1.2f, 1.2f, 1.5f, 2f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);


            addHeaderCell(table, "ID");
            addHeaderCell(table, "Cód. Bobina");
            addHeaderCell(table, "Producto");
            addHeaderCell(table, "OT");
            addHeaderCell(table, "P. Bruto");
            addHeaderCell(table, "P. Neto");
            addHeaderCell(table, "Fecha");
            addHeaderCell(table, "Operador");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


            for (BobinaTransitoDTO reg : registros) {
                addCellBody(table, String.valueOf(reg.getId()), Element.ALIGN_CENTER);
                addCellBody(table, reg.getCodigoBobina(), Element.ALIGN_LEFT);
                addCellBody(table, reg.getNombreProducto(), Element.ALIGN_LEFT);
                addCellBody(table, reg.getCodigoOT(), Element.ALIGN_CENTER);

                addNumericCell(table, String.format("%.2f", reg.getPesoBruto()), FONT_BODY);
                addNumericCell(table, String.format("%.2f", reg.getPesoNeto()), FONT_BODY);

                String fechaStr = reg.getFecha() != null ? reg.getFecha().format(dateFormatter) : "-";
                addCellBody(table, fechaStr, Element.ALIGN_CENTER);

                addCellBody(table, reg.getProducidoPor(), Element.ALIGN_LEFT);
            }


            PdfPCell cellTotalLabel = new PdfPCell(new Paragraph("TOTALES", FONT_TOTAL));
            cellTotalLabel.setColspan(4);
            cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalLabel.setPadding(5f);
            table.addCell(cellTotalLabel);

            addNumericCell(table, String.format("%.2f", totalBruto), FONT_TOTAL);
            addNumericCell(table, String.format("%.2f", totalNeto), FONT_TOTAL);

            PdfPCell cellTotalEmpty = new PdfPCell(new Paragraph("", FONT_TOTAL));
            cellTotalEmpty.setColspan(2);
            cellTotalEmpty.setPadding(5f);
            table.addCell(cellTotalEmpty);

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el reporte PDF", e);
        }

        return baos.toByteArray();
    }



    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.DARK_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Paragraph(text, FONT_HEADER));
        header.setPadding(6f);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(header);
    }

    private void addCellBody(PdfPTable table, String text, int align) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FONT_BODY));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addNumericCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    @Override
    @Transactional(readOnly = true)
    public Double obtenerStockTotal() {
        return bobinaRepository.sumarPesoNetoTotal();
    }
}
