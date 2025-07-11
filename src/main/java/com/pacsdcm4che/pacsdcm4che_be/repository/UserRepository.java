package com.pacsdcm4che.pacsdcm4che_be.repository;

import com.pacsdcm4che.pacsdcm4che_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);

    Optional<Object> findByUsername(String username);

}
