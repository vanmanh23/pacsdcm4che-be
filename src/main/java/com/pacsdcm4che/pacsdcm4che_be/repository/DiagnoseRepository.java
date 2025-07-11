package com.pacsdcm4che.pacsdcm4che_be.repository;

import com.pacsdcm4che.pacsdcm4che_be.entity.Diagnose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnoseRepository extends JpaRepository<Diagnose, Long>{
}
