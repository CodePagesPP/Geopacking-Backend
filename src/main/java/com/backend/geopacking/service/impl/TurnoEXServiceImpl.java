package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.RegistroTurnoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.OrdenTrabajoEXRepository;
import com.backend.geopacking.repository.TurnoEXRepository;
import com.backend.geopacking.service.InsumoService;
import com.backend.geopacking.service.InventarioService;
import com.backend.geopacking.service.TurnoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TurnoEXServiceImpl implements TurnoEXService {
    @Autowired
    private OrdenTrabajoEXRepository otRepository;
    @Autowired
    private TurnoEXRepository turnoRepository;
    @Autowired
    private InsumoService insumoService;
    @Autowired
    private InventarioService inventarioService;

    @Transactional(rollbackFor = Exception.class)
    public TurnoEX guardarTurno(RegistroTurnoDTO dto) {


        OrdenTrabajoEX ot = otRepository.findById(dto.getOtId())
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));


        TurnoEX turno = new TurnoEX();
        turno.setFechaHoraFin(LocalDateTime.now());
        turno.setOrdenTrabajo(ot);
        turno.setComentarios(dto.getComentarios());
        turno.setUsuarioNombre(dto.getUsuarioNombre());


        double totalKilos = 0;
        List<BobinaEX> bobinas = dto.getBobinas();
        if (bobinas != null) {
            for (BobinaEX b : bobinas) {
                b.setTurno(turno);
                totalKilos += b.getPesoNeto();
            }
        }
        turno.setBobinas(bobinas);
        turno.setCantidadBobinas(bobinas != null ? bobinas.size() : 0);
        turno.setTotalKilosProducidos(totalKilos);


        List<MaterialEX> materiales = dto.getMateriales();
        if (materiales != null) {
            for (MaterialEX m : materiales) {
                m.setTurno(turno);

                if (m.getMaterialOriginalId() != null && m.getCantidadKg() > 0) {
                    try {
                        insumoService.registrarSalidaAutomatica(m.getMaterialOriginalId(), m.getCantidadKg());
                    } catch (Exception e) {

                        System.err.println("Error descontando insumo: " + e.getMessage());
                    }
                }
            }
        }
        turno.setMateriales(materiales);


        List<ScrappEX> listaScrappParaGuardar = new ArrayList<>();
        if (dto.getScrapp() != null) {
            for (RegistroTurnoDTO.ScrappDTO sDto : dto.getScrapp()) {
                ScrappEX s = new ScrappEX();
                s.setTipo(sDto.getTipo());
                s.setCantidad(sDto.getCantidad());
                s.setTurno(turno);

                if (sDto.getTypeScrappId() != null && sDto.getCantidad() > 0) {
                    s.setTypeScrappOriginalId(sDto.getTypeScrappId());
                    try {
                        inventarioService.registrarSalidaAutomaticaScrapp(sDto.getTypeScrappId(), sDto.getCantidad());
                    } catch (Exception e) {
                        System.err.println("Error descontando Scrapp: " + e.getMessage());
                    }
                }
                listaScrappParaGuardar.add(s);
            }
        }
        turno.setScrapps(listaScrappParaGuardar);


        double producidoAnterior = ot.getProducidoKg() == null ? 0 : ot.getProducidoKg();
        double acumuladoActual = producidoAnterior + totalKilos;
        ot.setProducidoKg(acumuladoActual);

        if (acumuladoActual >= ot.getRequerimientoKg()) {
            ot.setEstado(EstadoOT_EX.COMPLETADO);
        } else {
            ot.setEstado(EstadoOT_EX.EN_PROCESO);
        }


        otRepository.save(ot);
        return turnoRepository.save(turno);
    }
}
