package com.toplms.master.users;

import com.toplms.domain.base.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Optional<Role> findByRoleName(String name) {
        try {
            name = name.trim().toUpperCase();
            return roleRepository.findByRoleName(name);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<Role> findByRoleId(String roleId) {
        try {
            roleId = roleId.trim().toUpperCase();
            return roleRepository.findById(roleId);
        }  catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isExistsByName(String name) {
        try {
            name = name.trim().toUpperCase();
            return !roleRepository.findByRoleName(name).isEmpty();
        }  catch (IllegalArgumentException e) {

            return false;
        }
    }

    public boolean existsByRoleId(String roleId) {
        try {
            roleId = roleId.trim().toUpperCase();
            return roleRepository.findById(roleId) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Role createRole(String name, Map<String, Object> menuJson) {
        try {

            Role role = roleRepository.save(new Role(name, menuJson));
            return role;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
