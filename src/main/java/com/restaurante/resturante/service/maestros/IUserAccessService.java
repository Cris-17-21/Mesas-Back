package com.restaurante.resturante.service.maestros;

import com.restaurante.resturante.dto.maestro.MasterRegistroDto;
import com.restaurante.resturante.dto.security.UserDto;

public interface IUserAccessService {
    UserDto registrarUsuarioAdmin(MasterRegistroDto dto);
}
