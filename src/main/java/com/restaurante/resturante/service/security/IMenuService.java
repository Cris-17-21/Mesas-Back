package com.restaurante.resturante.service.security;

import java.util.List;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.MenuModuleDto;

public interface IMenuService {

    List<MenuModuleDto> buildUserMenu(User user);

    List<MenuModuleDto> buildUserMenuByUsername(String username);
}
