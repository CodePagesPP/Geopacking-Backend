package com.backend.geopacking.dto;

import com.backend.geopacking.model.BobinaEX;
import com.backend.geopacking.model.MaterialEX;
import lombok.Data;
import java.util.List;

@Data
public class RegistroTurnoDTO {
    private Long otId;
    private List<BobinaEX> bobinas;
    private List<MaterialEX> materiales;
}
