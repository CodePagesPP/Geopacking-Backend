package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InventarioManualDTO;
import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.InventarioMovimientoRepository;
import com.backend.geopacking.repository.MotivoRepository;
import com.backend.geopacking.repository.TypeScrappRepository;
import com.backend.geopacking.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioMovimientoRepository inventarioRepository;
    private final TypeScrappRepository typeScrappRepository;
    private final MotivoRepository motivoRepository;

    @Override
    @Transactional
    public void registrarIngresoDesdeScrapp(Scrapp scrapp) {
        String codigo = scrapp.getNumeroBolson() + "/" + (scrapp.getAnio() % 100);

        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(codigo)
                .operacion(Operacion.INGRESO)
                .tipoRegistro(TipoRegistro.SCRAPP)
                .cantidad(scrapp.getPesoNeto()) // Usamos Peso Neto para inventario
                .fecha(scrapp.getFechaCreacion())
                .scrappReferencia(scrapp)
                .registradoPor(scrapp.getOperador())
                .typeScrapp(scrapp.getTypeScrapp())
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void registrarMovimientoManual(InventarioManualDTO dto, User adminUser) {
        int anioActual = LocalDate.now().getYear();

        TypeScrapp typeScrapp = typeScrappRepository.findById(dto.getTypeScrappId())
                .orElseThrow(() -> new EntityNotFoundException("TypeScrapp no encontrado"));

        Motivo motivo = null;
        if (dto.getNuevoMotivo() != null && !dto.getNuevoMotivo().isBlank()) {
            String nombre = dto.getNuevoMotivo().trim();
            motivo = motivoRepository.findByNombreIgnoreCase(nombre)
                    .orElseGet(() -> motivoRepository.save(Motivo.builder().nombre(nombre).build()));
        } else if (dto.getMotivoId() != null) {
            motivo = motivoRepository.findById(dto.getMotivoId())
                    .orElseThrow(() -> new EntityNotFoundException("Motivo no encontrado"));
        }

        Optional<InventarioMovimiento> ultimo = inventarioRepository
                .findUltimoManualPorTipoAnioYTipoScrapp(dto.getOperacion(), typeScrapp, anioActual);
        long nuevoSecuencial = 1;
        if (ultimo.isPresent()) {
            try {
                String[] partes = ultimo.get().getCodigoMovimiento().split("-");
                String seqStr = partes[2].split("/")[0];
                nuevoSecuencial = Long.parseLong(seqStr) + 1;
            } catch (Exception e) {
                nuevoSecuencial = inventarioRepository.count() + 1;
            }
        }

        String prefijo = (dto.getOperacion() == Operacion.INGRESO) ? "IN" : "SA";
        String codigo = String.format("%s-MOLPP-%d/%d", prefijo, nuevoSecuencial, anioActual);

        InventarioMovimiento mov = InventarioMovimiento.builder()
                .codigoMovimiento(codigo)
                .operacion(dto.getOperacion())
                .tipoRegistro(TipoRegistro.MANUAL)
                .cantidad(dto.getCantidad())
                .fecha(LocalDate.now())
                .registradoPor(adminUser)
                .typeScrapp(typeScrapp)
                .motivo(motivo)
                .nota(dto.getNota())
                .build();

        inventarioRepository.save(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioMovimientoDTO> listarMovimientos(LocalDate inicio, LocalDate fin, Long typeScrappId,Pageable pageable) {
        Page<InventarioMovimiento> paginaEntidades = inventarioRepository.findWithFilters(
                inicio,
                fin,
                typeScrappId,
                pageable
        );

        // Mapear de Entidad a DTO
        return paginaEntidades.map(this::mapToDTO);
    }

    private InventarioMovimientoDTO mapToDTO(InventarioMovimiento entidad) {
        InventarioMovimientoDTO dto = new InventarioMovimientoDTO();
        dto.setId(entidad.getId());
        dto.setCodigoMovimiento(entidad.getCodigoMovimiento());
        dto.setOperacion(entidad.getOperacion());
        dto.setTipoRegistro(entidad.getTipoRegistro());
        dto.setCantidad(entidad.getCantidad());
        dto.setFecha(entidad.getFecha());
        dto.setFechaRegistro(entidad.getFechaRegistro());
        dto.setNota(entidad.getNota());

        if (entidad.getRegistradoPor() != null) {
            dto.setRegistradoPorNombre(entidad.getRegistradoPor().getName());
        }

        if (entidad.getTypeScrapp() != null) {
            dto.setTypeScrappNombre(entidad.getTypeScrapp().getName());
        }

        if (entidad.getMotivo() != null) {
            dto.setMotivoNombre(entidad.getMotivo().getNombre());
        }

        return dto;
    }

    @Override
    public Double obtenerStockActual() {
        return inventarioRepository.calcularStockActual();
    }


    @Override
    public List<InventarioMovimientoDTO> listarMovimientosReporte(LocalDate inicio, LocalDate fin, Long typeScrappId) {

        Pageable unpaged = Pageable.unpaged();
        Page<InventarioMovimiento> pagina = inventarioRepository.findWithFilters(inicio, fin, typeScrappId, unpaged);
        return pagina.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public byte[] generarPdfDisenoImagen(List<InventarioMovimientoDTO> lista, String rangoFechas, String filtroInfo) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();


        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontCuerpoTabla = FontFactory.getFont(FontFactory.HELVETICA, 9);


        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2, 1});

        headerTable.addCell(crearCeldaSinBorde("", fontSubtitulo, Element.ALIGN_LEFT)); // Vacío
        headerTable.addCell(crearCeldaSinBorde("GEOPACKING S.A.C.", fontEmpresa, Element.ALIGN_CENTER));

        String fechaImpresion = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        headerTable.addCell(crearCeldaSinBorde(fechaImpresion, fontSubtitulo, Element.ALIGN_RIGHT));

        document.add(headerTable);
        document.add(new Paragraph(" "));


        PdfPTable infoTable = new PdfPTable(3);
        infoTable.setWidthPercentage(100);


        infoTable.addCell(crearCeldaInfo("LOCAL:", "PLANTA PRINCIPAL", fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("FECHA FILTRO:", rangoFechas, fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("REPORTE:", "HISTORIAL KARDEX", fontLabel, fontValue));


        infoTable.addCell(crearCeldaInfo("MOVIMIENTO:", "ENTRADAS / SALIDAS", fontLabel, fontValue));
        infoTable.addCell(crearCeldaInfo("USUARIO:", "ADMINISTRADOR", fontLabel, fontValue)); // O el usuario actual
        infoTable.addCell(crearCeldaInfo("FILTRO:", filtroInfo, fontLabel, fontValue));

        document.add(infoTable);
        document.add(new Paragraph(" "));

        Paragraph tituloTabla = new Paragraph("DETALLES DEL MOVIMIENTO", fontLabel);
        tituloTabla.setAlignment(Element.ALIGN_CENTER);
        document.add(tituloTabla);
        document.add(new Paragraph(" "));


        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f, 2f, 2f, 5f, 2f});


        String[] headers = {"ITEM", "FECHA REG", "CÓDIGO", "OPERACIÓN", "DESCRIPCIÓN (SCRAPP/MOTIVO)", "CANTIDAD"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            cell.setBorderWidth(1.2f);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }


        int item = 1;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (InventarioMovimientoDTO mov : lista) {

            addDataCell(table, String.valueOf(item++), fontCuerpoTabla, Element.ALIGN_CENTER);


            String fechaStr = mov.getFechaRegistro() != null ? mov.getFechaRegistro().format(dtfFecha) : "-";
            addDataCell(table, fechaStr, fontCuerpoTabla, Element.ALIGN_CENTER);


            addDataCell(table, mov.getCodigoMovimiento(), fontCuerpoTabla, Element.ALIGN_CENTER);


            String op = mov.getOperacion() != null ? mov.getOperacion().toString() : "-";
            addDataCell(table, op, fontCuerpoTabla, Element.ALIGN_CENTER);


            String desc = (mov.getTypeScrappNombre() != null ? mov.getTypeScrappNombre() : "") +
                    " - " +
                    (mov.getMotivoNombre() != null ? mov.getMotivoNombre() : "");
            addDataCell(table, desc, fontCuerpoTabla, Element.ALIGN_LEFT);


            addDataCell(table, df.format(mov.getCantidad()) + " KG", fontCuerpoTabla, Element.ALIGN_RIGHT);
        }

        document.add(table);


        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        Paragraph footer = new Paragraph("----------------------------------------\nCONFIRMADO", fontValue);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }


    private PdfPCell crearCeldaSinBorde(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private PdfPCell crearCeldaInfo(String label, String value, Font fLabel, Font fValue) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", fLabel));
        phrase.add(new Chunk(value, fValue));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private void addDataCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
