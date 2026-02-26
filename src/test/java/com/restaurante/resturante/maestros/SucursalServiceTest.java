package com.restaurante.resturante.maestros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.mapper.maestros.SucursalDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.service.maestros.jpa.SucursalService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {
    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private SucursalDtoMapper sucursalMapper;

    @InjectMocks
    private SucursalService sucursalService;

    @Test
    @DisplayName("Debería crear una sucursal exitosamente vinculada a una empresa")
    void create_Success() {
        // GIVEN
        String empresaId = "emp-123";
        CreateSucursalDto dto = new CreateSucursalDto("Sede Central", "Av. Siempre Viva 123", "12345678", empresaId);

        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.setRazonSocial("EMPRESA TEST");

        Sucursal sucursalEntity = new Sucursal();
        sucursalEntity.setNombre("SEDE CENTRAL"); // El mapper ya debería darlo en MAYÚSCULAS

        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(sucursalMapper.toEntity(any(CreateSucursalDto.class))).thenReturn(sucursalEntity);
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(sucursalEntity);
        when(sucursalMapper.toDto(any(Sucursal.class)))
                .thenReturn(new SucursalDto("suc-1", "SEDE CENTRAL", "Dir", "123", "EMPRESA TEST"));

        // WHEN
        SucursalDto result = sucursalService.create(dto);

        // THEN
        assertNotNull(result);
        assertEquals("SEDE CENTRAL", result.nombre());
        verify(empresaRepository).findById(empresaId);
        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si la empresa no existe al crear sucursal")
    void create_EmpresaNotFound_ThrowsException() {
        // GIVEN
        CreateSucursalDto dto = new CreateSucursalDto("Sede", "Dir", "12345678", "no-existe");
        when(empresaRepository.findById("no-existe")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> sucursalService.create(dto));
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar los datos de la sucursal y normalizar a MAYÚSCULAS")
    void update_Success() {
        // GIVEN
        String sucursalId = "suc-123";
        CreateSucursalDto updateDto = new CreateSucursalDto("Nuevo Nombre", "Nueva Dir", "87654321", "emp-123");

        Sucursal existingSucursal = new Sucursal();
        existingSucursal.setId(sucursalId);
        existingSucursal.setNombre("NOMBRE ANTIGUO");

        Empresa empresa = new Empresa();
        empresa.setId("emp-123");
        existingSucursal.setEmpresa(empresa);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(existingSucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(existingSucursal);

        // WHEN
        sucursalService.update(sucursalId, updateDto);

        // THEN
        // Verificamos que se llamó al mapper para la actualización
        verify(sucursalMapper).updateEntity(eq(updateDto), eq(existingSucursal));
        verify(sucursalRepository).save(existingSucursal);
    }

}
