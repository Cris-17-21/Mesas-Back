package com.restaurante.resturante.service.maestros.jpa;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.maestros.Cliente;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.maestro.ClienteDto;
import com.restaurante.resturante.dto.maestro.CreateClienteDto;
import com.restaurante.resturante.mapper.maestros.ClienteDtoMapper;
import com.restaurante.resturante.repository.maestro.ClienteRepository;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.security.TipoDocumentoRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionClienteService;
import com.restaurante.resturante.service.maestros.IClienteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ClienteDtoMapper clienteMapper;
    private final FacturacionClienteService facturacionClienteService;

    public Cliente getOrCreateClienteByDocument(String numeroDocumento, String nombreRazonSocial,
            String direccion, Empresa empresa) {
        return clienteRepository.findByNumeroDocumentoAndEmpresaId(numeroDocumento, empresa.getId())
                .map(cliente -> {
                    if (!cliente.getActive()) {
                        cliente.setActive(true);
                        cliente.setNombreRazonSocial(nombreRazonSocial);
                        cliente.setDireccion(direccion);
                        return clienteRepository.save(cliente);
                    }
                    return cliente;
                })
                .orElseGet(() -> {
                    Cliente nuevo = Cliente.builder()
                            .numeroDocumento(numeroDocumento)
                            .nombreRazonSocial(nombreRazonSocial)
                            .direccion(direccion)
                            .empresa(empresa)
                            .tipoDocumento(tipoDocumentoRepository
                                    .findByName(determinarTipoDoc(numeroDocumento))
                                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado")))
                            .active(true)
                            .build();
                    Cliente saved = clienteRepository.save(nuevo);
                    syncWithApi(saved);
                    return saved;
                });
    }

    private String determinarTipoDoc(String numeroDocumento) {
        if (numeroDocumento == null)
            return "DNI";
        if (numeroDocumento.length() == 11)
            return "RUC";
        return "DNI";
    }

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

                    nuevoCliente.setTipoDocumento(tipoDocumentoRepository.findByName(dto.tipoDocumento())
                            .orElseThrow(() -> new RuntimeException("Tipo de Documento no encontrado")));

                    nuevoCliente.setActive(true); // Aseguramos que nazca activo
                    Cliente saved = clienteRepository.save(nuevoCliente);
                    syncWithApi(saved);
                    return clienteMapper.toDto(saved);
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
        entidad.setTipoDocumento(tipoDocumentoRepository.findByName(dto.tipoDocumento())
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

    private void syncWithApi(Cliente cliente) {
        try {
            facturacionClienteService.syncCliente(cliente);
        } catch (Exception e) {
            log.warn("No se pudo sincronizar cliente con API: {}", e.getMessage());
        }
    }
}
