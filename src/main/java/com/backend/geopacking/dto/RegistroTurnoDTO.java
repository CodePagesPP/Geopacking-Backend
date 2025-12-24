package com.backend.geopacking.dto;

import com.backend.geopacking.model.BobinaEX;
import com.backend.geopacking.model.MaterialEX;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.TypeScrapp;
import lombok.Data;
import java.util.List;

@Data
public class RegistroTurnoDTO {
    private Long otId;
    private List<BobinaEX> bobinas;
    private List<MaterialEX> materiales;
    private List<ScrappDTO> scrapp;
    private String comentarios;
    private String usuarioNombre;

    @Data
    public static class ScrappDTO {
        private String tipo;
        private Double cantidad;
    }
}
