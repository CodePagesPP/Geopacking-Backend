package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.OrdenTrabajoTFDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.OrdenTrabajoTFRepository;
import com.backend.geopacking.repository.ProductoTFRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.OrdenTrabajoTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        ot.setCodigo("OT-TF-" + String.format("%04d", correlativo));

        OrdenTrabajoTF ordenGuardada = otRepository.save(ot);

        return mapToDTO(ordenGuardada);
    }

    @Override
    public List<OrdenTrabajoTFDTO> listarOrdenes() {
        return otRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
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

    private OrdenTrabajoTFDTO mapToDTO(OrdenTrabajoTF entity) {
        OrdenTrabajoTFDTO dto = new OrdenTrabajoTFDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setRequerimientoKg(entity.getRequerimientoKg());
        dto.setProducidoKg(entity.getProducidoKg());
        dto.setEstado(entity.getEstado());
        dto.setPrioridad(entity.getPrioridad());

        // Relaciones
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
