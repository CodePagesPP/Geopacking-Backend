package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.OrdenTrabajoEXRepository;
import com.backend.geopacking.repository.ProductoEXRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.OrdenTrabajoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenTrabajoEXServiceImpl implements OrdenTrabajoEXService{

    @Autowired
    OrdenTrabajoEXRepository otRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private ProductoEXRepository productoRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public OrdenTrabajoEXDTO crearOrden(OrdenTrabajoEXDTO dto, String dniUsuario) {
        OrdenTrabajoEX ot =  new OrdenTrabajoEX();

        Maquina maquina = maquinaRepository.findById(dto.getMaquinaId())
                .orElseThrow(() -> new RuntimeException("Máquina no encontrada con ID: " + dto.getMaquinaId()));

        ProductoEX producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + dto.getProductoId()));

        User usuario = userRepository.findByDni(dniUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con dni: " + dniUsuario));

        ot.setMaquina(maquina);
        ot.setProducto(producto);
        ot.setRequerimientoKg(dto.getRequerimientoKg());
        ot.setCreadaPor(usuario);

        Integer maxPrioridad = otRepository.findMaxPrioridad();

        ot.setPrioridad(maxPrioridad + 1);

        long correlativo = otRepository.count() + 1;
        ot.setCodigo("OT-EX-" + String.format("%04d", correlativo));

        OrdenTrabajoEX ordenGuardada = otRepository.save(ot);

        return mapToDTO(ordenGuardada);
    }

    @Override
    public List<OrdenTrabajoEXDTO> listarOrdenes() {
        List<OrdenTrabajoEX> ordenes = otRepository.findAll();
        return ordenes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    @Override
    public List<OrdenTrabajoEXDTO> listarOrdenesOT() {

        List<OrdenTrabajoEX> ordenes = otRepository.findAll(Sort.by(Sort.Direction.ASC, "prioridad"))
                .stream()
                .filter(ot -> !ot.getEstado().equals(EstadoOT_EX.COMPLETADO))
                .collect(Collectors.toList());

        return ordenes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajoEXDTO> listarOrdenesPrioridad() {
        List<OrdenTrabajoEX> ordenes = otRepository.findAll(Sort.by(Sort.Direction.ASC, "prioridad"));
        return ordenes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void actualizarPrioridades(List<OrdenTrabajoEXDTO> listaOrdenada) {

        for (int i = 0; i < listaOrdenada.size(); i++) {
            OrdenTrabajoEXDTO dto = listaOrdenada.get(i);


            OrdenTrabajoEX ot = otRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("OT no encontrada id: " + dto.getId()));


            ot.setPrioridad(i + 1);


            otRepository.save(ot);
        }
    }

    private OrdenTrabajoEXDTO mapToDTO(OrdenTrabajoEX entity) {
        OrdenTrabajoEXDTO dto = new OrdenTrabajoEXDTO();

        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setFechaCreacion(entity.getFechaCreacion());

        dto.setRequerimientoKg(entity.getRequerimientoKg());
        dto.setProducidoKg(entity.getProducidoKg());
        dto.setEstado(entity.getEstado());

        dto.setMaquinaId(entity.getMaquina().getId());
        dto.setProductoId(entity.getProducto().getId());
        dto.setCreadaPor(entity.getCreadaPor().getName());

        dto.setMaquinaNombre(entity.getMaquina().getModelo());
        dto.setProductoNombre(entity.getProducto().getName());
        dto.setCreadaPorUsername(entity.getCreadaPor().getName());
        dto.setPrioridad(entity.getPrioridad());
        dto.setMaterialesProducto(entity.getProducto().getMateriales());
        return dto;
    }
}
