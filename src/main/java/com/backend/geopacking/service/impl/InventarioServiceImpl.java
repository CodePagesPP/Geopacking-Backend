package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InventarioManualDTO;
import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.*;
import com.backend.geopacking.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioMovimientoRepository inventarioRepository;
    private final TypeScrappRepository typeScrappRepository;
    private final MotivoRepository motivoRepository;
    private final UserRepository userRepository;
    private final OrdenTrabajoEXRepository otExRepository;

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

        if (entidad.getOrdenTrabajo() != null) {
            dto.setCodigoOT(entidad.getOrdenTrabajo().getCodigo());
        } else {
            dto.setCodigoOT("-");
        }

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSalidaAutomaticaScrapp(Long typeScrappId, Double cantidad, Long otId) {
        TypeScrapp tipo = typeScrappRepository.findById(typeScrappId)
                .orElseThrow(() -> new RuntimeException("Tipo de Scrapp no encontrado"));

        OrdenTrabajoEX ot = null;
        String codigoOTStr = "GENERAL";

        if (otId != null) {
            ot = otExRepository.findById(otId).orElse(null);
            if (ot != null) codigoOTStr = ot.getCodigo();
        }

        Motivo motivoProd = motivoRepository.findByNombreIgnoreCase("PRODUCCION")
                .orElseThrow(() -> new RuntimeException("Motivo PRODUCCION no existe"));

        User usuarioSistema = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuario Sistema (ID 1) no encontrado"));

        String codigoGen = "SAL-PROD-" + codigoOTStr;

        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(codigoGen)
                .operacion(Operacion.SALIDA)
                .tipoRegistro(TipoRegistro.PRODUCCION)
                .cantidad(cantidad)
                .fecha(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .typeScrapp(tipo)
                .motivo(motivoProd)
                .registradoPor(usuarioSistema)
                .nota("Consumo automático por Producción")
                .ordenTrabajo(ot)
                .build();

        inventarioRepository.save(movimiento);
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

        Document document = new Document(PageSize.A4, 20, 20, 20, 20); // Márgenes reducidos
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();


        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontCuerpoTabla = FontFactory.getFont(FontFactory.HELVETICA, 9);


        InventarioMovimientoDTO headerData = lista.isEmpty() ? new InventarioMovimientoDTO() : lista.get(0);



        DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String valFechaMov = LocalDate.now().format(dtfFecha);

        String valAlmacen = "ALMACÉN TRANSITO EX";
        String valMovimiento = (headerData.getOperacion() != null) ? headerData.getOperacion().toString() : "INGRESO/SALIDA";
        String valNumMov = (headerData.getId() != null) ? String.valueOf(headerData.getId()) : "123";
        String valComentarios = (headerData.getNota() != null) ? headerData.getNota() : "";

        String valUsuario = (headerData.getRegistradoPorNombre() != null) ? headerData.getRegistradoPorNombre() : "ADMIN";
        String valMotivo = (headerData.getMotivoNombre() != null) ? headerData.getMotivoNombre() : "-";



        Paragraph titulo = new Paragraph("GEOPACKING SAC", fontEmpresa);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(15f);
        document.add(titulo);


        PdfPTable infoTable = new PdfPTable(8);
        infoTable.setWidthPercentage(100);


        infoTable.setWidths(new float[]{2.5f, 4f, 2.5f, 3f, 1.5f, 1.5f, 3f, 4f});


        addCellHeaderInfo(infoTable, "ALMACÉN", fontLabel);
        addCellHeaderInfo(infoTable, valAlmacen, fontValue);


        addCellHeaderInfo(infoTable, "MOVIMIENTO", fontLabel);
        addCellHeaderInfo(infoTable, valMovimiento, fontValue);


        addCellHeaderInfo(infoTable, "N° MOV", fontLabel);
        addCellHeaderInfo(infoTable, valNumMov, fontValue);


        addCellHeaderInfo(infoTable, "COMENTARIOS", fontLabel);
        addCellHeaderInfo(infoTable, valComentarios, fontValue);


        addCellHeaderInfo(infoTable, "USUARIO", fontLabel);
        addCellHeaderInfo(infoTable, valUsuario, fontValue);


        addCellHeaderInfo(infoTable, "FECHA DEL MOV.", fontLabel);
        addCellHeaderInfo(infoTable, valFechaMov, fontValue);


        addCellHeaderInfo(infoTable, "MOTIVO", fontLabel);
        addCellHeaderInfo(infoTable, valMotivo, fontValue);


        addCellHeaderInfo(infoTable, "", fontLabel);
        addCellHeaderInfo(infoTable, "", fontValue);

        document.add(infoTable);
        document.add(new Paragraph(" "));


        Paragraph tituloTabla = new Paragraph("DETALLES DEL MOVIMIENTO", fontLabel);
        tituloTabla.setAlignment(Element.ALIGN_CENTER);
        tituloTabla.setSpacingAfter(5f);
        document.add(tituloTabla);


        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        table.setWidths(new float[]{1f, 2f, 2f, 2f, 4.5f, 2.5f});


        String[] headers = {"ITEM", "FECHA REG", "CÓDIGO", "OPERACIÓN", "SCRAPP / MOTIVO", "CANTIDAD"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));


            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);

            cell.setBorderWidthTop(1.5f);
            cell.setBorderWidthBottom(1.5f);
            cell.setBorderWidthLeft(0);
            cell.setBorderWidthRight(0);

            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }


        int item = 1;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (InventarioMovimientoDTO mov : lista) {

            addDataCellClean(table, String.valueOf(item++), fontCuerpoTabla, Element.ALIGN_CENTER);

            String fechaRegStr = mov.getFechaRegistro() != null ? mov.getFechaRegistro().format(dtfFecha) : "-";
            addDataCellClean(table, fechaRegStr, fontCuerpoTabla, Element.ALIGN_CENTER);

            addDataCellClean(table, mov.getCodigoMovimiento(), fontCuerpoTabla, Element.ALIGN_CENTER);

            String op = mov.getOperacion() != null ? mov.getOperacion().toString() : "-";
            addDataCellClean(table, op, fontCuerpoTabla, Element.ALIGN_CENTER);

            String desc = (mov.getTypeScrappNombre() != null ? mov.getTypeScrappNombre() : "-");
            addDataCellClean(table, desc, fontCuerpoTabla, Element.ALIGN_LEFT);

            addDataCellClean(table, df.format(mov.getCantidad()) + " KG", fontCuerpoTabla, Element.ALIGN_RIGHT);
        }


        PdfPCell lineaFinal = new PdfPCell();
        lineaFinal.setColspan(6);
        lineaFinal.setBorder(Rectangle.TOP);
        lineaFinal.setBorderWidthTop(1.5f);
        table.addCell(lineaFinal);

        document.add(table);


        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("----------------------------------------\nCONFIRMADO", fontValue);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }


    private void addCellHeaderInfo(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingBottom(5f);
        table.addCell(cell);
    }


    private void addDataCellClean(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);


        cell.setBorder(Rectangle.NO_BORDER);


        table.addCell(cell);
    }
}
