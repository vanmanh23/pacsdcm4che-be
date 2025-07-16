package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.dtos.*;
import com.pacsdcm4che.pacsdcm4che_be.entity.*;
import com.pacsdcm4che.pacsdcm4che_be.service.DiagnoseService;
import com.pacsdcm4che.pacsdcm4che_be.service.DicomClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/dicom")
@CrossOrigin(origins = "*")
public class DicomController {
    @Autowired
    private DicomClientService dicomClientService;
    @Autowired
    private DiagnoseService diagnoseService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDicom(@RequestParam("file") MultipartFile[] file) throws IOException {
        ArrayList<Map<String, String> > listRespone = new ArrayList<Map<String, String> >();
        for (MultipartFile multipartFile : file) {
            Map<String, String>  respon = dicomClientService.uploadDicomFile(multipartFile);
            listRespone.add(respon);
        }
        return ResponseEntity.ok(listRespone.toString());
    }

    @GetMapping("/studies/tags")
    public ResponseEntity<?> getStudyTags() {
            List<StudyDTO> studyDTOList = dicomClientService.getStudyByUID();
            return ResponseEntity.ok(studyDTOList);
    }

    @GetMapping("/studies/{studyInstanceUID}/series/tags")
    public ResponseEntity<?> getSeriesTags(@PathVariable String studyInstanceUID) {
            List<SeriesDTO> seriesList = dicomClientService.getSeriesByStudyUID(studyInstanceUID);
            return ResponseEntity.ok(seriesList);
    }

    @GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances/tags")
    public ResponseEntity<?> getInstanceTags(@PathVariable String studyInstanceUID, @PathVariable String seriesInstanceUID) {
        List<InstanceDTO> instancesList = dicomClientService.getInstancesBySeriesUidAndStudyUid(studyInstanceUID, seriesInstanceUID);
        return ResponseEntity.ok(instancesList);
    }

    @GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances/{instanceUID}/images")
    public ResponseEntity<byte[]> getInstanceImages(
            @PathVariable String studyInstanceUID,
            @PathVariable String seriesInstanceUID,
            @PathVariable String instanceUID) {

            ResponseEntity<byte[]> response = dicomClientService.getInstancesImage(studyInstanceUID, seriesInstanceUID, instanceUID);
            return response;

    }
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients () {
            List<PatientDTO> patientsList = dicomClientService.getPatients();
            return ResponseEntity.ok(patientsList);
    }

    @PutMapping ("/diagnose")
    public ResponseEntity<Diagnose> createDiagnose(@RequestBody DiagnoseDTO diagnoseDTO) {
        Diagnose createdDiagnose = diagnoseService.updateDescription(diagnoseDTO);
        return new ResponseEntity<>(createdDiagnose, HttpStatus.OK);
    }
}