package com.pacsdcm4che.pacsdcm4che_be.service;

import com.pacsdcm4che.pacsdcm4che_be.entity.Diagnose;
import com.pacsdcm4che.pacsdcm4che_be.repository.DiagnoseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagnoseService {
    @Autowired
    private DiagnoseRepository diagnoseRepository;

    public Diagnose saveDiagnose(Diagnose diagnose) {
        try {
            return diagnoseRepository.save(diagnose);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
