package com.restaurante.resturante.maestros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.mapper.maestros.EmpresaDtoMapper;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.MedioPagoRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.repository.security.UserRepository;
import com.restaurante.resturante.service.api_facturacion.FacturacionAuthService;
import com.restaurante.resturante.service.api_facturacion.FacturacionEmpresaService.FacturacionEmpresaService;
import com.restaurante.resturante.service.maestros.jpa.EmpresaService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {
    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EmpresaDtoMapper empresaMapper;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private UserAccessRepository userAccessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacturacionAuthService facturacionAuthService;

    @Mock
    private FacturacionEmpresaService facturacionEmpresaService;

    @Mock
    private MedioPagoRepository medioPagoRepository;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    @DisplayName("Debería crear una empresa exitosamente con Razón Social en MAYÚSCULAS")
    void create_Success() {
        // GIVEN: Datos de entrada y comportamiento esperado
        CreateEmpresaDto dto = new CreateEmpresaDto("20123456789", "Restaurante Mi Sazón", "Nombre Comercial",
                "Calle Real 123", "010101", "Prov", "Dep", "Dist",
                "987654321", "contacto@sazon.com", "logo.png", "2026-02-26", null, null, null, null, null);

        Empresa empresaEntity = new Empresa();
        empresaEntity.setRuc("20123456789");
        empresaEntity.setRazonSocial("RESTAURANTE MI SAZÓN"); // Ya normalizada por el mapper

        when(empresaMapper.toEntity(any(CreateEmpresaDto.class))).thenReturn(empresaEntity);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaEntity);
        when(empresaMapper.toDto(any(Empresa.class)))
                .thenReturn(new EmpresaDto("1", "20123456789", "RESTAURANTE MI SAZÓN", "Nombre Comercial",
                        "Calle Real 123", "010101", "Prov", "Dep", "Dist", "987654321",
                        "contacto@sazon.com", "logo.png", "2026-02-26", null, null, null, null, null,
                        java.util.Collections.emptyList()));

        // WHEN: Ejecutamos la acción
        EmpresaDto result = empresaService.create(dto);

        // THEN: Verificamos resultados
        assertNotNull(result);
        assertEquals("RESTAURANTE MI SAZÓN", result.razonSocial());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el RUC ya está registrado")
    void create_RucExists_ThrowsException() {
        // GIVEN
        CreateEmpresaDto dto = new CreateEmpresaDto("20123456789", "Test", null, "Dir", "010101", "Prov", "Dep", "Dist",
                "123", "a@a.com", "url",
                "2026-01-01", null, null, null, null, null);
        Empresa existingEmpresa = new Empresa();
        existingEmpresa.setActive(true);
        when(empresaRepository.findByRuc("20123456789")).thenReturn(Optional.of(existingEmpresa));

        // WHEN & THEN
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> empresaService.create(dto));
        assertTrue(ex.getMessage().contains("ya está registrado"));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar solo los campos enviados (Update Parcial/Defensivo)")
    void update_PartialUpdate_Success() {
        // GIVEN
        String id = "uuid-123";
        // Enviamos un DTO donde solo cambia el email, el RUC es el mismo
        CreateEmpresaDto updateDto = new CreateEmpresaDto("20123456789", "RAZON SOCIAL", null, null, null, null, null,
                null, null, "nuevo@email.com",
                null, null, null, null, null, null, null);

        Empresa existingEmpresa = new Empresa();
        existingEmpresa.setId(id);
        existingEmpresa.setRuc("20123456789");
        existingEmpresa.setRazonSocial("RAZON SOCIAL");

        when(empresaRepository.findById(id)).thenReturn(Optional.of(existingEmpresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(existingEmpresa);

        // WHEN
        empresaService.update(id, updateDto);

        // THEN: El service debe llamar al mapper para la actualización parcial
        verify(empresaMapper).updateEntityFromDto(eq(updateDto), eq(existingEmpresa));
        verify(empresaRepository).save(existingEmpresa);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException cuando el ID no existe")
    void findById_NotFound_ThrowsException() {
        // GIVEN
        String id = "id-no-existe";
        when(empresaRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> empresaService.findById(id));
    }
}
