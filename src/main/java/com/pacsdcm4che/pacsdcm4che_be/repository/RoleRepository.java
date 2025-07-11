package com.pacsdcm4che.pacsdcm4che_be.repository;

import com.pacsdcm4che.pacsdcm4che_be.entity.ERole;
import com.pacsdcm4che.pacsdcm4che_be.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByName(ERole name);
}
