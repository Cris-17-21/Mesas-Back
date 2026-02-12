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
        // 1. Buscamos el registro existente (activo o inactivo)
        return clienteRepository.findByNumeroDocumentoAndEmpresaId(dto.numeroDocumento(), dto.empresaId())
                .map(clienteExistente -> {
                    // Si ya está activo, lanzamos la excepción que tenías
                    if (clienteExistente.getActive()) {
                        throw new RuntimeException(
                                "El documento " + dto.numeroDocumento() + " ya está registrado y activo.");
                    }

                    // Si existe pero estaba INACTIVO, lo actualizamos y reactivamos
                    actualizarDatosCliente(clienteExistente, dto);
                    clienteExistente.setActive(true);

                    return clienteMapper.toDto(clienteRepository.save(clienteExistente));
                })
                .orElseGet(() -> {
                    // 2. Si no existe en absoluto, creación limpia
                    Cliente nuevoCliente = clienteMapper.toEntity(dto);

                    nuevoCliente.setEmpresa(empresaRepository.findById(dto.empresaId())
                            .orElseThrow(() -> new RuntimeException("Empresa no encontrada")));

                    nuevoCliente.setTipoDocumento(tipoDocumentoRepository.findById(dto.tipoDocumentoId())
                            .orElseThrow(() -> new RuntimeException("Tipo de Documento no encontrado")));

                    nuevoCliente.setActive(true); // Aseguramos que nazca activo
                    return clienteMapper.toDto(clienteRepository.save(nuevoCliente));
                });
    }

    // Método auxiliar para no repetir código de seteo
    private void actualizarDatosCliente(Cliente entidad, CreateClienteDto dto) {
        entidad.setNombreRazonSocial(dto.nombreRazonSocial());
        entidad.setDireccion(dto.direccion());
        entidad.setCorreo(dto.correo());
        entidad.setTelefono(dto.telefono());
        // También actualizamos el tipo por si cambió de DNI a RUC con el mismo número
        // (raro, pero posible)
        entidad.setTipoDocumento(tipoDocumentoRepository.findById(dto.tipoDocumentoId())
                .orElseThrow(() -> new RuntimeException("Tipo de Documento no encontrado")));
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
