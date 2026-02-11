package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.dto.maestro.ClienteDto;
import com.restaurante.resturante.dto.maestro.CreateClienteDto;
import com.restaurante.resturante.mapper.maestros.ClienteDtoMapper;
import com.restaurante.resturante.repository.maestro.ClienteRepository;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.service.maestros.IClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ClienteDtoMapper clienteMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> listarPorEmpresa(String empresaId) {
        return clienteRepository.findByEmpresaIdAndActiveTrue(empresaId)
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ClienteDto crear(CreateClienteDto dto) {
        // Validación de duplicados por empresa
        if (clienteRepository.existsByNumeroDocumentoAndEmpresaId(dto.numeroDocumento(), dto.empresaId())) {
            throw new RuntimeException(
                    "El documento " + dto.numeroDocumento() + " ya está registrado en esta empresa.");
        }

        Cliente cliente = clienteMapper.toEntity(dto);

        // Seteo de relaciones desde el DTO
        cliente.setEmpresa(empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada")));

        cliente.setTipoDocumento(tipoDocumentoRepository.findById(dto.tipoDocumentoId())
                .orElseThrow(() -> new RuntimeException("Tipo de Documento no encontrado")));

        return clienteMapper.toDto(clienteRepository.save(cliente));
    }

    @Override
    public ClienteDto buscarPorDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .map(clienteMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Override
    @Transactional
    public ClienteDto actualizar(String id, CreateClienteDto dto) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setNombreRazonSocial(dto.nombreRazonSocial().toUpperCase());
        cliente.setDireccion(dto.direccion());
        cliente.setCorreo(dto.correo());
        cliente.setTelefono(dto.telefono());
        return clienteMapper.toDto(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(String id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setActive(false);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto obtenerPorId(String id) {
        return clienteRepository.findById(id)
                .map(clienteMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}
