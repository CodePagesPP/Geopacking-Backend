package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ClienteDTO;
import com.backend.geopacking.model.Cliente;
import com.backend.geopacking.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_ACCESS')")
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<Cliente>> getClientesPaginados(@RequestParam(required = false) String filtro,
                                                              @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        Page<Cliente> pagina = clienteService.getClientesPaginados(filtro, pageable);
        return ResponseEntity.ok(pagina);
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
