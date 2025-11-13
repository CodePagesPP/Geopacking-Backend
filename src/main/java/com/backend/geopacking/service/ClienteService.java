package com.backend.geopacking.service;

import com.backend.geopacking.dto.ClienteDTO;
import com.backend.geopacking.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {
    Page<Cliente> getClientesPaginados(String filtro, Pageable pageable);
    Cliente getClienteById(Long id);
    Cliente createCliente(ClienteDTO clienteDTO);
    Cliente updateCliente(Long id, ClienteDTO clienteDTO);
    void deleteCliente(Long id);
    Cliente getClienteByNumeroDocumento(String numeroDocumento);
    List<Cliente> getClientesParaExportar(String filtro);
}
