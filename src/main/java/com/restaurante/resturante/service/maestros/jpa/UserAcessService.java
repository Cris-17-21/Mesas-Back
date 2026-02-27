package com.restaurante.resturante.service.maestros.jpa;

import org.springframework.stereotype.Service;

import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;
import com.restaurante.resturante.dto.maestro.MasterRegistroDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.UserDto;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.service.maestros.IEmpresaService;
import com.restaurante.resturante.service.maestros.ISucursalService;
import com.restaurante.resturante.service.maestros.IUserAccessService;
import com.restaurante.resturante.service.security.IUserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAcessService implements IUserAccessService {

    private final IEmpresaService empresaService;
    private final ISucursalService sucursalService;
    private final IUserService userService;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDto registrarUsuarioAdmin(MasterRegistroDto dto) {
        // 1. Crear la Empresa (Obtenemos el ID generado)
        EmpresaDto empresaSaved = empresaService.create(dto.empresa());

        // 2. Crear la Sucursal vinculándola manualmente con la Empresa
        CreateSucursalDto sucursalReq = new CreateSucursalDto(
                dto.sucursal().nombre(),
                dto.sucursal().direccion(),
                dto.sucursal().telefono(),
                empresaSaved.id() // Forzamos la relación con el ID real
        );
        SucursalDto sucursalSaved = sucursalService.create(sucursalReq);

        // 3. Obtener el ID del Rol ADMIN_RESTAURANTE
        // Lo buscamos por nombre para obtener su UUID/ID actual
        String adminRoleId = roleRepository.findByName("ADMIN_RESTAURANTE")
                .map(Role::getId)
                .orElseThrow(() -> new EntityNotFoundException("Error: Rol ADMIN_RESTAURANTE no configurado en BD."));

        // 4. Crear el Usuario Administrador Re-construimos el DTO del usuario para
        // inyectar los IDs de Empresa, Sucursal y Rol
        CreateUserDto adminUserReq = new CreateUserDto(
                dto.user().username(),
                dto.user().password(),
                dto.user().nombres(),
                dto.user().apellidoPaterno(),
                dto.user().apellidoMaterno(),
                dto.user().tipoDocumento(),
                dto.user().numeroDocumento(),
                dto.user().telefono(),
                dto.user().email(),
                adminRoleId, // ID del Rol
                empresaSaved.id(), // ID de Empresa
                sucursalSaved.id() // ID de Sucursal
        );

        // 5. El userService se encargará de crear el User y el UserAccess (Atómico)
        return userService.create(adminUserReq);
    }

}
