package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.*;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.*;
import com.backend.geopacking.service.OrdenTrabajoTFService;
import com.backend.geopacking.service.PdfTfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenTrabajoTFServiceImpl implements OrdenTrabajoTFService {

    @Autowired
    private OrdenTrabajoTFRepository otRepository;
    @Autowired
    private MaquinaRepository maquinaRepository;
    @Autowired
    private ProductoTFRepository productoTFRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DetalleProduccionTFRepository detalleRepository;
    @Autowired
    private BobinaEXRepository bobinaRepository;
    @Autowired
    private InventarioCajaRepository inventarioRepository;
    @Autowired
    private PdfTfService pdfService;
    @Autowired
    private MovimientoSalidaRepository movimientoRepository;
    @Autowired
    private DetalleMovimientoSalidaRepository detalleMovimientoSalidaRepository;

    @Override
    public OrdenTrabajoTFDTO crearOrden(OrdenTrabajoTFDTO dto, String dniUsuario) {
        OrdenTrabajoTF ot = new OrdenTrabajoTF();

        Maquina maquina = maquinaRepository.findById(dto.getMaquinaId())
                .orElseThrow(() -> new RuntimeException("Máquina no encontrada"));

        ProductoTF producto = productoTFRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto TF no encontrado"));

        User usuario = userRepository.findByDni(dniUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ot.setMaquina(maquina);
        ot.setProducto(producto);
        ot.setRequerimientoKg(dto.getRequerimientoKg());
        ot.setCreadaPor(usuario);

        Integer maxPrioridad = otRepository.findMaxPrioridad();
        ot.setPrioridad(maxPrioridad == null ? 1 : maxPrioridad + 1);

        long correlativo = otRepository.count() + 1;
        ot.setCodigo("OP-TF-" + String.format("%04d", correlativo));

        OrdenTrabajoTF ordenGuardada = otRepository.save(ot);

        return mapToDTO(ordenGuardada);
    }

    @Override
    public List<OrdenTrabajoTFDTO> listarOrdenes() {
        return otRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrdenTrabajoTFDTO> listarPaginado(
            Long maquinaId,
            Long productoId,
            String estadoStr,
            LocalDate fDesde,
            LocalDate fHasta,
            Pageable pageable) {


        EstadoOT_TF estado = null;
        if (estadoStr != null && !estadoStr.isEmpty()) {
            try {
                estado = EstadoOT_TF.valueOf(estadoStr);
            } catch (IllegalArgumentException e) {

            }
        }


        LocalDateTime desde = (fDesde != null) ? fDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fHasta != null) ? fHasta.atTime(LocalTime.MAX) : null;


        Page<OrdenTrabajoTF> page = otRepository.filtrarOrdenes(maquinaId, productoId, estado, desde, hasta, pageable);


        return page.map(this::mapToDTO);
    }

    @Override
    public List<OrdenTrabajoTFDTO> listarOrdenesPrioridad() {
        return otRepository.findAll(Sort.by(Sort.Direction.ASC, "prioridad"))
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void actualizarPrioridades(List<OrdenTrabajoTFDTO> listaOrdenada) {
        for (int i = 0; i < listaOrdenada.size(); i++) {
            OrdenTrabajoTFDTO dto = listaOrdenada.get(i);
            OrdenTrabajoTF ot = otRepository.findById(dto.getId()).orElse(null);
            if (ot != null) {
                ot.setPrioridad(i + 1);
                otRepository.save(ot);
            }
        }
    }

    @Override
    public List<OrdenTrabajoTFDTO> listarOrdenesPendientes() {
        return otRepository.findAll(Sort.by(Sort.Direction.ASC, "prioridad"))
                .stream()
                .filter(ot -> !ot.getEstado().equals(EstadoOT_TF.COMPLETADO))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarOrden(Long idOrden) {
        OrdenTrabajoTF ot = otRepository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrado con codigo: " + idOrden));

        otRepository.delete(ot);
    }

    @Override
    public OrdenTrabajoTFDTO actualizarOrden(Long idOrden, OrdenTrabajoTFDTO dto) {
        OrdenTrabajoTF ot = otRepository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrado con codigo: " + idOrden));

        if (dto.getMaquinaId() != null) {
            Maquina nuevaMaquina = maquinaRepository.findById(dto.getMaquinaId())
                    .orElseThrow(() -> new RuntimeException("Máquina no encontrada con ID: " + dto.getMaquinaId()));
            ot.setMaquina(nuevaMaquina);
        }

        if (dto.getProductoId() != null) {
            ProductoTF nuevoProducto = productoTFRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto TF no encontrado con ID: " + dto.getProductoId()));
            ot.setProducto(nuevoProducto);
        }

        if (dto.getRequerimientoKg() != null && dto.getRequerimientoKg() > 0) {
            ot.setRequerimientoKg(dto.getRequerimientoKg());
        }

        OrdenTrabajoTF ordenGuardada = otRepository.save(ot);

        return mapToDTO(ordenGuardada);
    }

    @Override
    @Transactional
    public List<DetalleProduccionTF> registrarAvance(List<RegistroProduccionTFDTO> dtos, String username) {
        List<DetalleProduccionTF> listaGuardada = new ArrayList<>();

        for (RegistroProduccionTFDTO dto : dtos) {
            OrdenTrabajoTF ot = otRepository.findById(dto.getOtId())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + dto.getOtId()));

            LocalTime inicio = (dto.getHoraInicio() != null && !dto.getHoraInicio().isEmpty())
                    ? LocalTime.parse(dto.getHoraInicio()) : LocalTime.now();
            LocalTime fin = (dto.getHoraFin() != null && !dto.getHoraFin().isEmpty())
                    ? LocalTime.parse(dto.getHoraFin()) : LocalTime.now();

            DetalleProduccionTF detalle = DetalleProduccionTF.builder()
                    .ordenTrabajo(ot)
                    .codigoBobina(dto.getCodigoBobina())
                    .loteBobina(dto.getLoteBobina())
                    .velocidad(dto.getVelocidad())
                    .horaInicio(inicio)
                    .horaFin(fin)
                    .cajas(dto.getCajas())
                    .rechazoKg(dto.getRechazoKg())
                    .pesoPromedio(dto.getPesoPromedio())
                    .bobinaFin(dto.getBobinaFin())
                    .fechaRegistro(LocalDate.now())
                    .registradoPor(username)
                    .build();

            DetalleProduccionTF detalleGuardado = detalleRepository.save(detalle);
            listaGuardada.add(detalleGuardado);

            if (dto.getCajas() != null && dto.getCajas() > 0) {
                InventarioCaja inv = InventarioCaja.builder()
                        .detalleProduccion(detalleGuardado)
                        .loteProduccion(ot.getCodigo()) // Lote = Codigo OT
                        .nombreProducto(ot.getProducto().getName())
                        .cantidad(dto.getCajas())
                        .fechaProduccion(LocalDateTime.now())
                        .estado("EN_TF")
                        .build();

                inventarioRepository.save(inv);
            }

            if (Boolean.TRUE.equals(dto.getBobinaFin())) {
                // Buscamos la bobina por su código
                bobinaRepository.findByCodigoIgnoreCase(dto.getCodigoBobina())
                        .ifPresent(bobina -> {
                            bobina.setEstado("CONSUMIDA");
                            bobinaRepository.save(bobina);
                        });
            }

            double producidoActual = (ot.getProducidoKg() != null) ? ot.getProducidoKg() : 0;
            double nuevoTotal = producidoActual + dto.getCajas();

            ot.setProducidoKg(nuevoTotal);

            if (nuevoTotal >= ot.getRequerimientoKg()) {
                ot.setEstado(EstadoOT_TF.COMPLETADO);
            } else {
                ot.setEstado(EstadoOT_TF.EN_PROCESO);
            }

            otRepository.save(ot);
        }

        return listaGuardada;
    }

    @Override
    public void enviarAProductosTerminados(Long idInventario) {
        InventarioCaja item = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        item.setEstado("EN_PT");
        inventarioRepository.save(item);
    }

    @Override
    public Page<InventarioCajaDTO> listarInventarioPaginado(
            String estado,
            LocalDate fInicio,
            LocalDate fFin,
            String busqueda,
            Pageable pageable) {

        // 1. Preparar Fechas (Igual que antes)
        LocalDateTime inicio = (fInicio != null) ? fInicio.atStartOfDay() : null;
        LocalDateTime fin = (fFin != null) ? fFin.atTime(LocalTime.MAX) : null;

        // 2. CORRECCIÓN AQUÍ: Preparar el término con % y minúsculas
        String term = null;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // Agregamos % al inicio y fin, y convertimos a minúscula
            term = "%" + busqueda.trim().toLowerCase() + "%";
        }

        // 3. Llamada al Repositorio (Pasa 'term' que ya tiene los %)
        Page<InventarioCaja> pagina = inventarioRepository.filtrarInventario(estado, inicio, fin, term, pageable);

        // 4. Mapeo (Igual que antes)
        return pagina.map(item -> {
            String codigoReal = "-";

            if (item.getDetalleProduccion() != null
                    && item.getDetalleProduccion().getOrdenTrabajo() != null
                    && item.getDetalleProduccion().getOrdenTrabajo().getProducto() != null) {

                codigoReal = item.getDetalleProduccion().getOrdenTrabajo().getProducto().getCode();
            }

            return InventarioCajaDTO.builder()
                    .id(item.getId())
                    .loteProduccion(item.getLoteProduccion())
                    .codProducto(codigoReal)
                    .nombreProducto(item.getNombreProducto())
                    .cantidad(item.getCantidad())
                    .fechaProduccion(item.getFechaProduccion())
                    .estado(item.getEstado())
                    .build();
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Integer obtenerStockTotal(
            String estado,
            LocalDate fInicio,
            LocalDate fFin,
            String busqueda) {

        // 1. Preparar Fechas (Igual que en listar)
        LocalDateTime inicio = (fInicio != null) ? fInicio.atStartOfDay() : null;
        LocalDateTime fin = (fFin != null) ? fFin.atTime(LocalTime.MAX) : null;

        // 2. Preparar Búsqueda (Igual que en listar)
        String term = null;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            term = "%" + busqueda.trim().toLowerCase() + "%";
        }

        // 3. Llamar al Repo de Suma
        return inventarioRepository.sumarStockTotal(estado, inicio, fin, term);
    }

    @Override
    public BobinaInfoDTO buscarBobinaPorCodigo(String codigo) {
        String codigoLimpio = codigo.trim();

        System.out.println("Buscando bobina con código limpio: '" + codigoLimpio + "'");

        BobinaEX bobina = bobinaRepository.findByCodigoIgnoreCase(codigoLimpio)
                .orElseThrow(() -> new RuntimeException("Bobina no encontrada con código: " + codigoLimpio));

        String lote = (bobina.getTurno() != null && bobina.getTurno().getOrdenTrabajo() != null) ?
                bobina.getTurno().getOrdenTrabajo().getCodigo() : "S/L";

        String nombreProd = "Desconocido";
        if (bobina.getTurno() != null &&
                bobina.getTurno().getOrdenTrabajo().getProducto() != null) {
            nombreProd = bobina.getTurno().getOrdenTrabajo().getProducto().getName();
        }

        return BobinaInfoDTO.builder()
                .id(bobina.getId())
                .codigo(bobina.getCodigo())
                .lote(lote)
                .nombreProducto(nombreProd)
                .pesoNeto(bobina.getPesoNeto())
                .build();
    }

    @Override
    public List<HistorialCajasDTO> listarHistorial(LocalDate inicio, LocalDate fin) {
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaRegistro", "horaFin");
        List<DetalleProduccionTF> detalles;

        if (inicio != null && fin != null) {
            detalles = detalleRepository.findByFechaRegistroBetween(inicio, fin, sort);
        } else {
            detalles = detalleRepository.findAll(sort);
        }

        return detalles.stream().map(d -> {

            String nombreMostrar = d.getRegistradoPor(); // Por defecto dejamos el DNI

            if (d.getRegistradoPor() != null) {
                Optional<User> usuarioOpt = userRepository.findByDni(d.getRegistradoPor());
                if (usuarioOpt.isPresent()) {
                    User u = usuarioOpt.get();
                    nombreMostrar = u.getName() + " " + u.getLastName();
                }
            }

            return HistorialCajasDTO.builder()
                    .id(d.getId())
                    .otId(d.getOrdenTrabajo().getId())
                    .fecha(d.getFechaRegistro())
                    .hora(d.getHoraFin())
                    .otCodigo(d.getOrdenTrabajo().getCodigo())
                    .producto(d.getOrdenTrabajo().getProducto().getName())
                    .codigoBobina(d.getCodigoBobina())
                    .loteBobina(d.getLoteBobina())
                    .cajas(d.getCajas())
                    .operador(nombreMostrar)
                    .inicioSecuencia(1)
                    .build();
        }).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public byte[] registrarSalidaMasiva(SalidaRequestDTO request, String username) {
        // 1. Guardar Cabecera Historial
        MovimientoSalida movimiento = MovimientoSalida.builder()
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(username)
                .motivo(request.getMotivo())
                .comentarios(request.getComentarios())
                .build();

        movimiento = movimientoRepository.save(movimiento);

        // Lista para enviar al generador de PDF (DTOs limpios)
        List<ReporteDetalleSalidaDTO> filasPdf = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 2. Procesar Items
        for (TfSalidaDTO dto : request.getItems()) {
            InventarioCaja item = inventarioRepository.findById(dto.getInventarioId())
                    .orElseThrow(() -> new RuntimeException("Item no encontrado"));

            if (item.getCantidad() < dto.getCantidadRetirar()) {
                throw new RuntimeException("Stock insuficiente: " + item.getLoteProduccion());
            }

            // Actualizar stock
            item.setCantidad(item.getCantidad() - dto.getCantidadRetirar());
            if (item.getCantidad() == 0) {
                item.setEstado("DESPACHADO");
            }
            inventarioRepository.save(item);

            // Obtener Código Producto
            String codProd = "-";
            if (item.getDetalleProduccion().getOrdenTrabajo().getProducto() != null) {
                codProd = item.getDetalleProduccion().getOrdenTrabajo().getProducto().getCode();
            }

            // Guardar Detalle Historial
            DetalleMovimientoSalida det = DetalleMovimientoSalida.builder()
                    .movimiento(movimiento)
                    .codigoProducto(codProd)
                    .nombreProducto(item.getNombreProducto())
                    .loteProduccion(item.getLoteProduccion())
                    .cantidad(dto.getCantidadRetirar())
                    .fechaProduccion(item.getFechaProduccion())
                    .build();
            detalleMovimientoSalidaRepository.save(det);

            // 3. Agregar a lista para PDF (Mapeo a DTO)
            filasPdf.add(ReporteDetalleSalidaDTO.builder()
                    .fecha(item.getFechaProduccion() != null ? item.getFechaProduccion().format(dtf) : "-")
                    .codigo(codProd)
                    .lote(item.getLoteProduccion())
                    .cantidad(String.valueOf(dto.getCantidadRetirar()))
                    .build());
        }

        // 4. Generar PDF Unificado
        return pdfService.generarReporteSalida(
                username,
                request.getMotivo(),
                request.getComentarios(),
                filasPdf
        );
    }

    @Override
    public Page<MovimientoSalida> listarHistorialSalidas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        if (fechaInicio != null && fechaFin != null) {

            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

            return movimientoRepository.findByFechaRegistroBetween(inicio, fin, pageable);
        } else {
            // Sin filtros, solo paginación
            return movimientoRepository.findAll(pageable);
        }
    }

    @Override
    public byte[] reimprimirReporteSalida(Long movimientoId) {
        MovimientoSalida mov = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<ReporteDetalleSalidaDTO> filasPdf = mov.getDetalles().stream()
                .map(det -> ReporteDetalleSalidaDTO.builder()
                        .fecha(det.getFechaProduccion() != null ? det.getFechaProduccion().format(dtf) : "-")
                        .codigo(det.getCodigoProducto())
                        .lote(det.getLoteProduccion())
                        .cantidad(String.valueOf(det.getCantidad()))
                        .build())
                .collect(Collectors.toList());

        return pdfService.generarReporteSalida(
                mov.getRegistradoPor(),
                mov.getMotivo(),
                mov.getComentarios(),
                filasPdf
        );
    }

    @Override
    @Transactional
    public void iniciarOrden(Long id) {
        OrdenTrabajoTF ot = otRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));

        // Solo cambiamos si está en ESPERA
        if (ot.getEstado() == EstadoOT_TF.EN_ESPERA) {
            ot.setEstado(EstadoOT_TF.EN_PROCESO);
            otRepository.save(ot);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<InventarioCajaDTO> buscarInventarioPorCodigoProducto(String codigo) {

        List<InventarioCaja> entidades = inventarioRepository.buscarPorCodigoProducto(codigo);


        return entidades.stream()
                .map(item -> {
                    String codigoReal = "-";

                    if (item.getDetalleProduccion() != null
                            && item.getDetalleProduccion().getOrdenTrabajo() != null
                            && item.getDetalleProduccion().getOrdenTrabajo().getProducto() != null) {
                        codigoReal = item.getDetalleProduccion().getOrdenTrabajo().getProducto().getCode();
                    }

                    return InventarioCajaDTO.builder()
                            .id(item.getId())
                            .loteProduccion(item.getLoteProduccion())
                            .codProducto(codigoReal)
                            .nombreProducto(item.getNombreProducto())
                            .cantidad(item.getCantidad())
                            .fechaProduccion(item.getFechaProduccion())
                            .estado(item.getEstado())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private OrdenTrabajoTFDTO mapToDTO(OrdenTrabajoTF entity) {
        OrdenTrabajoTFDTO dto = new OrdenTrabajoTFDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setRequerimientoKg(entity.getRequerimientoKg());
        dto.setProducidoKg(entity.getProducidoKg());
        dto.setEstado(entity.getEstado());
        dto.setPrioridad(entity.getPrioridad());
        dto.setEmpaque(entity.getProducto().getLinea());

        if (entity.getMaquina() != null) {
            dto.setMaquinaId(entity.getMaquina().getId());
            dto.setMaquinaNombre(entity.getMaquina().getModelo());
        }

        if (entity.getProducto() != null) {
            dto.setProductoId(entity.getProducto().getId());
            dto.setProductoNombre(entity.getProducto().getName());

            if (entity.getProducto().getProductoBase() != null) {
                dto.setProductoBaseNombre(entity.getProducto().getProductoBase().getName());
            } else {
                dto.setProductoBaseNombre("N/A");
            }
        }

        if (entity.getCreadaPor() != null) {
            dto.setCreadaPorUsername(entity.getCreadaPor().getName());
        }

        return dto;
    }
}
