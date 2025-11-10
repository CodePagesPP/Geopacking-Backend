package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ClienteDTO;
import com.backend.geopacking.model.Cliente;
import com.backend.geopacking.repository.ClienteRepository;
import com.backend.geopacking.service.ClienteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> getClientesPaginados(String filtro, Pageable pageable) {
        if (filtro != null && !filtro.trim().isEmpty()) {
            return clienteRepository.findByFiltro(filtro, pageable);
        } else {
            return clienteRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente getClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente getClienteByNumeroDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con Documento: " + numeroDocumento));
    }

    @Override
    @Transactional
    public Cliente createCliente(ClienteDTO clienteDTO) {
        Optional<Cliente> existente = clienteRepository.findByNumeroDocumento(clienteDTO.getNumeroDocumento());
        if (existente.isPresent()) {
            throw new ValidationException("Ya existe un cliente con el número de documento: " + clienteDTO.getNumeroDocumento());
        }

        Cliente cliente = mapDtoToEntity(clienteDTO, new Cliente());
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente updateCliente(Long id, ClienteDTO clienteDTO) {
        Cliente clienteExistente = getClienteById(id);

        if (!clienteExistente.getNumeroDocumento().equals(clienteDTO.getNumeroDocumento())) {
            Optional<Cliente> otro = clienteRepository.findByNumeroDocumento(clienteDTO.getNumeroDocumento());
            if (otro.isPresent()) {
                throw new ValidationException("El nuevo número de documento ya pertenece a otro cliente.");
            }
        }

        Cliente clienteActualizado = mapDtoToEntity(clienteDTO, clienteExistente);
        return clienteRepository.save(clienteActualizado);
    }

    @Override
    @Transactional
    public void deleteCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private Cliente mapDtoToEntity(ClienteDTO dto, Cliente entity) {
        entity.setTipoCliente(dto.getTipoCliente());
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombre(dto.getNombre());
        entity.setNombreComercial(dto.getNombreComercial());
        entity.setPais(dto.getPais() != null ? dto.getPais() : "PERU");
        entity.setDepartamento(dto.getDepartamento());
        entity.setProvincia(dto.getProvincia());
        entity.setDistrito(dto.getDistrito());
        entity.setDireccion(dto.getDireccion());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());
        entity.setFechaNacimiento(dto.getFechaNacimiento());
        entity.setCreditoHabilitado(dto.isCreditoHabilitado());
        entity.setMontoCredito(dto.getMontoCredito());
        entity.setObservacion(dto.getObservacion());
        entity.setContactoNombre(dto.getContactoNombre());
        entity.setContactoTelefono(dto.getContactoTelefono());
        return entity;
    }
}
