package com.backend.geopacking;

import com.backend.geopacking.model.Permission;
import com.backend.geopacking.model.RoleE;
import com.backend.geopacking.repository.PermissionRepository;
import com.backend.geopacking.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;


    @Override
    public void run(String... args) {
        System.out.println("Iniciando carga de data inicial...");

        try {
            createPermissionsAndRoles();
            System.out.println("Data inicial cargada correctamente.");
        } catch (Exception e) {
            System.err.println("Error al cargar data inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void createPermissionsAndRoles() {
        // PERMISOOOOOOOOSSS
        Permission adminPermission = createPermissionIfNotExists("ADMIN_ACCESS");
        Permission instructorPermission = createPermissionIfNotExists("INSTRUCTOR_ACCESS");
        Permission staffPermission = createPermissionIfNotExists("STAFF_ACCESS");
        Permission clientPermission = createPermissionIfNotExists("CLIENT_ACCESS");
        Permission managerPermission = createPermissionIfNotExists("MANAGER_ACCESS");
        Permission rolePermission = createPermissionIfNotExists("ROLE_ACCESS");
        Permission roomPermission = createPermissionIfNotExists("ROOM_ACCESS");
        Permission disciplinePermission = createPermissionIfNotExists("DISCIPLINE_ACCESS");
        Permission classSessionPermission = createPermissionIfNotExists("CLASS_SESSION_ACCESS");
        Permission excelPermission = createPermissionIfNotExists("EXCEL_ACCESS");
        Permission paymentPermission = createPermissionIfNotExists("PAYMENT_ACCESS");
        // Roles con sus permisos
        createRoleIfNotExists("ADMIN", "ROL ADMIN", Set.of(adminPermission));
        createRoleIfNotExists("CLIENT", "ROL CLIENT", Set.of(clientPermission));
        createRoleIfNotExists("INSTRUCTOR", "ROL INSTRUCTOR", Set.of(instructorPermission));
        createRoleIfNotExists("STAFF", "ROL STAFF", Set.of(staffPermission));
        createRoleIfNotExists("MANAGER", "ROL MANAGER", Set.of(managerPermission));

    }



    private Permission createPermissionIfNotExists(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private void createRoleIfNotExists(String name, String description,Set<Permission> permissions) {
        if (roleRepository.findByName(name).isEmpty()) {
            RoleE role = new RoleE();
            role.setName(name);
            role.setDescription(description);
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }
}
