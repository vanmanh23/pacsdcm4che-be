package com.pacsdcm4che.pacsdcm4che_be.service;

import com.pacsdcm4che.pacsdcm4che_be.dtos.DiagnoseDTO;
import com.pacsdcm4che.pacsdcm4che_be.entity.Diagnose;
import com.pacsdcm4che.pacsdcm4che_be.exception.ResourceNotFoundException;
import com.pacsdcm4che.pacsdcm4che_be.repository.DiagnoseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnoseService {
    @Autowired
    private DiagnoseRepository diagnoseRepository;

    public Diagnose updateDescription(DiagnoseDTO diagnoseDTO) {
            Diagnose diagnose = diagnoseRepository.findByStudyId(diagnoseDTO.getStudyId())
                    .orElseThrow(() -> new RuntimeException("Diagnose not found for study ID: " + diagnoseDTO.getStudyId()));
            diagnose.setDescription(diagnoseDTO.getDescription());
            return diagnoseRepository.save(diagnose);

    }
    public List<DiagnoseDTO> getAllDiagnose(){
        return diagnoseRepository.findAll().stream().map(diagnose -> new DiagnoseDTO(diagnose.getId(), diagnose.getDescription(), diagnose.getStudyId())).toList();
    }
    public Diagnose getDiagnoseByStudyId(String studyId) {
        return diagnoseRepository.findByStudyId(studyId).orElseThrow(() -> new RuntimeException("Diagnose not found for study ID: " + studyId));
    }
}
