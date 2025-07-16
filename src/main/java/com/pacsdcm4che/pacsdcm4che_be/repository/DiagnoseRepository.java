package com.pacsdcm4che.pacsdcm4che_be.repository;

import com.pacsdcm4che.pacsdcm4che_be.entity.Diagnose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnoseRepository extends JpaRepository<Diagnose, Long>{
    boolean existsByStudyId(String studyInstanceUID);
    Optional<Diagnose> findByStudyId(String studyInstanceUID);
}
