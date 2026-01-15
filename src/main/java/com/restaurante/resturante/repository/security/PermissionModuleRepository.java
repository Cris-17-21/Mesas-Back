package com.restaurante.resturante.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.resturante.domain.security.PermissionModule;

@Repository
public interface PermissionModuleRepository extends JpaRepository<PermissionModule, Long>{

    Optional<PermissionModule> findByName(String name);

    boolean existsByName(String name);

    boolean existsByPermissions_Module_Id(Long moduleId);

    boolean existsByParent_Id(Long parentId);

    List<PermissionModule> findByChildrenIsEmpty();
}
