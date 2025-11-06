package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.PaginatedScrappReportDTO;
import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.model.Maquina;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.ScrappRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.InventarioService;
import com.backend.geopacking.service.ScrappService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrappServiceImpl implements ScrappService {
    private final ScrappRepository registroScrappRepository;
    private final MaquinaRepository maquinaRepository;
    private final UserRepository userRepository;
    private final InventarioService  inventarioService;

    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

    @Override
    @Transactional
    public Scrapp registrarScrapp(ScrappDTO dto, UserDetails userDetails) {


        User user = userRepository.findByDni(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Maquina maquina = maquinaRepository.findById(dto.getMaquinaId())
                .orElseThrow(() -> new EntityNotFoundException("Máquina no encontrada"));


        int anioActual = LocalDate.now().getYear();


        Optional<Scrapp> ultimoRegistro = registroScrappRepository
                .findFirstByAnioOrderByNumeroBolsonDesc(anioActual);


        long nuevoNumeroBolson = ultimoRegistro.isPresent()
                ? ultimoRegistro.get().getNumeroBolson() + 1
                : 1;


        Scrapp nuevoRegistro = Scrapp.builder()
                .numeroBolson(nuevoNumeroBolson)
                .anio(anioActual)
                .pesoBruto(dto.getPesoBruto())
                .pesoNeto(dto.getPesoNeto())
                .fechaCreacion(LocalDate.now())
                .turno(obtenerTurnoActual())
                .maquina(maquina)
                .operador(user)
                .build();

        Scrapp scrappGuardado = registroScrappRepository.save(nuevoRegistro);

        inventarioService.registrarIngresoDesdeScrapp(scrappGuardado);

        return scrappGuardado;
    }

    @Override
    public List<Scrapp> obtenerTodos() {
        return registroScrappRepository.findAll();
    }

    @Override
    public Scrapp obtenerPorId(Long id) {
        return registroScrappRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de Scrapp no encontrado"));
    }

    private String obtenerTurnoActual() {
        int hora = LocalTime.now().getHour();
        if (hora < 14) return "M";
        else if (hora < 22) return "T";
        else return "N";
    }


    @Override
    @Transactional(readOnly = true)
    public PaginatedScrappReportDTO obtenerReporteAdmin(
            Pageable pageable,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            UserDetails userDetails) {

        Page<Scrapp> paginaDeRegistros;
        double totalBruto;
        double totalNeto;


        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN_ACCESS"));

        if (isAdmin) {


            if (fechaInicio != null && fechaFin != null) {
                paginaDeRegistros = registroScrappRepository
                        .findAllByFechaCreacionBetween(fechaInicio, fechaFin, pageable);
                totalBruto = registroScrappRepository.sumPesoBrutoBetween(fechaInicio, fechaFin);
                totalNeto = registroScrappRepository.sumPesoNetoBetween(fechaInicio, fechaFin);
            } else {
                paginaDeRegistros = registroScrappRepository.findAll(pageable);
                totalBruto = registroScrappRepository.sumPesoBruto();
                totalNeto = registroScrappRepository.sumPesoNeto();
            }

        } else {


            User operador = userRepository.findByDni(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("Operador no encontrado con DNI: " + userDetails.getUsername()));


            if (fechaInicio != null && fechaFin != null) {

                paginaDeRegistros = registroScrappRepository
                        .findAllByOperadorAndFechaCreacionBetween(operador, fechaInicio, fechaFin, pageable);
                totalBruto = registroScrappRepository.sumPesoBrutoByOperadorBetween(operador, fechaInicio, fechaFin);
                totalNeto = registroScrappRepository.sumPesoNetoByOperadorBetween(operador, fechaInicio, fechaFin);
            } else {

                paginaDeRegistros = registroScrappRepository.findAllByOperador(operador, pageable);
                totalBruto = registroScrappRepository.sumPesoBrutoByOperador(operador);
                totalNeto = registroScrappRepository.sumPesoNetoByOperador(operador);
            }
        }


        PaginatedScrappReportDTO dto = new PaginatedScrappReportDTO();
        dto.setRegistros(paginaDeRegistros.getContent());
        dto.setCurrentPage(paginaDeRegistros.getNumber());
        dto.setTotalItems(paginaDeRegistros.getTotalElements());
        dto.setTotalPages(paginaDeRegistros.getTotalPages());
        dto.setTotalPesoBruto(totalBruto);
        dto.setTotalPesoNeto(totalNeto);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReporteCompletoPdf(LocalDate fechaInicio, LocalDate fechaFin) {

        List<Scrapp> registros;
        double totalBruto;
        double totalNeto;
        String subtitulo;

        if (fechaInicio != null && fechaFin != null) {
            registros = registroScrappRepository
                    .findAllByFechaCreacionBetween(fechaInicio, fechaFin, Sort.by(Sort.Direction.DESC, "id"));
            totalBruto = registroScrappRepository.sumPesoBrutoBetween(fechaInicio, fechaFin);
            totalNeto = registroScrappRepository.sumPesoNetoBetween(fechaInicio, fechaFin);
            subtitulo = "Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " al " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            registros = registroScrappRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            totalBruto = registroScrappRepository.sumPesoBruto();
            totalNeto = registroScrappRepository.sumPesoNeto();
            subtitulo = "Todos los registros";
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36); // A4 con márgenes

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Paragraph titulo = new Paragraph("Reporte Completo de Scrapp", FONT_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            // NUEVO: Subtítulo con rango de fechas
            Paragraph pSubtitulo = new Paragraph(subtitulo, FONT_BODY);
            pSubtitulo.setAlignment(Element.ALIGN_CENTER);
            pSubtitulo.setSpacingAfter(15f);
            document.add(pSubtitulo);


            float[] columnWidths = {0.6f, 1f, 1f, 1.2f, 1.2f, 1.1f, 0.8f, 2f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);


            addHeaderCell(table, "ID");
            addHeaderCell(table, "Bolsón N°");
            addHeaderCell(table, "Máquina");
            addHeaderCell(table, "P. Bruto (Kg)");
            addHeaderCell(table, "P. Neto (Kg)");
            addHeaderCell(table, "Fecha");
            addHeaderCell(table, "Turno");
            addHeaderCell(table, "Operador");


            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Scrapp reg : registros) {
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(reg.getId()), FONT_BODY)));
                table.addCell(new PdfPCell(new Paragraph(reg.getNumeroBolson() + "/" + (reg.getAnio() % 100), FONT_BODY)));
                table.addCell(new PdfPCell(new Paragraph(reg.getMaquina().getCodigo(), FONT_BODY)));


                addNumericCell(table, String.format("%.2f", reg.getPesoBruto()), FONT_BODY);
                addNumericCell(table, String.format("%.2f", reg.getPesoNeto()), FONT_BODY);

                table.addCell(new PdfPCell(new Paragraph(reg.getFechaCreacion().format(dateFormatter), FONT_BODY)));
                table.addCell(new PdfPCell(new Paragraph(reg.getTurno(), FONT_BODY)));
                table.addCell(new PdfPCell(new Paragraph(reg.getOperador().getName(), FONT_BODY))); // Asumo que User tiene getName()
            }

            PdfPCell cellTotalLabel = new PdfPCell(new Paragraph("TOTALES", FONT_TOTAL));
            cellTotalLabel.setColspan(3);
            cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalLabel.setPadding(5f);
            table.addCell(cellTotalLabel);


            addNumericCell(table, String.format("%.2f", totalBruto), FONT_TOTAL);

            addNumericCell(table, String.format("%.2f", totalNeto), FONT_TOTAL);

            PdfPCell cellTotalEmpty = new PdfPCell(new Paragraph("", FONT_TOTAL));
            cellTotalEmpty.setColspan(3);
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
        header.setPadding(5f);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(header);
    }


    private void addNumericCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
