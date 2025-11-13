package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ClienteDTO;
import com.backend.geopacking.model.Cliente;
import com.backend.geopacking.service.ClienteService;
import com.backend.geopacking.service.impl.ClientExcelReport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;
    private final ClientExcelReport clientExcelReport;
    @GetMapping
    public ResponseEntity<Page<Cliente>> getClientesPaginados(@RequestParam(required = false) String filtro,
                                                              @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        Page<Cliente> pagina = clienteService.getClientesPaginados(filtro, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportClientesToExcel(
            @RequestParam(required = false) String filtro) throws IOException {


        List<Cliente> clientes = clienteService.getClientesParaExportar(filtro);


        ByteArrayInputStream bais = clientExcelReport.clientesToExcel(clientes);


        HttpHeaders headers = new HttpHeaders();
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String filename = "Reporte_Clientes_" + fecha + ".xlsx";


        headers.add("Content-Disposition", "attachment; filename=" + filename);

        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(bais));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findClienteById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getClienteById(id));
    }

    @GetMapping("/documento/{numero}")
    public ResponseEntity<Cliente> findClienteByNumeroDocumento(@PathVariable String numero) {
        return ResponseEntity.ok(clienteService.getClienteByNumeroDocumento(numero));
    }

    @PostMapping
    public ResponseEntity<Cliente> createCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        Cliente clienteNuevo =  clienteService.createCliente(clienteDTO);
        return new ResponseEntity<>(clienteNuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        Cliente clienteActualizado = clienteService.updateCliente(id, clienteDTO);
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Cliente> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }
}
