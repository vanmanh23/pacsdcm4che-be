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
    public ResponseEntity<?> uploadDicom(@RequestParam("file") MultipartFile[] file) {
        ArrayList<Map<String, String> > listRespone = new ArrayList<Map<String, String> >();
        try {
            for (MultipartFile multipartFile : file) {
                Map<String, String>  respon = dicomClientService.uploadDicomFile(multipartFile);
                listRespone.add(respon);
            }
            return ResponseEntity.ok(listRespone.toString());
//            return ResponseEntity.ok("update dicom file successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
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
    public ResponseEntity<?> getInstanceTags(
            @PathVariable String studyInstanceUID,
            @PathVariable String seriesInstanceUID) {
            List<InstanceDTO> instancesList = dicomClientService.getInstancesBySeriesUidAndStudyUid(studyInstanceUID, seriesInstanceUID);
            return ResponseEntity.ok(instancesList);
    }
    @GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances/{instanceUID}/images")
    public ResponseEntity<List<String>> getInstanceImages(
            @PathVariable String studyInstanceUID,
            @PathVariable String seriesInstanceUID,
            @PathVariable String instanceUID) {
            ResponseEntity<List<String>> respon = dicomClientService.getInstancesImage(studyInstanceUID, seriesInstanceUID, instanceUID);
            return respon;
    }
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients () {
            List<PatientDTO> patientsList = dicomClientService.getPatients();
            return ResponseEntity.ok(patientsList);
    }

    @PutMapping("/diagnose")
    public ResponseEntity<Diagnose> createDiagnose(@RequestBody DiagnoseDTO diagnoseDTO) {
        Diagnose createdDiagnose = diagnoseService.updateDescription(diagnoseDTO);
        return new ResponseEntity<>(createdDiagnose, HttpStatus.OK);
    }
    @GetMapping("/instances")
    public ResponseEntity<?> getInstances () {
            List<InstanceDTO> instancesList = dicomClientService.searchForInstances();
            return ResponseEntity.ok(instancesList);
    }
    @GetMapping("/studies/count")
    public ResponseEntity<?> getStudyCount() {
            StudyCountDTO res = dicomClientService.countStudies();
            return ResponseEntity.ok(res);
    }
    @GetMapping("/studies/size")
    public ResponseEntity<?> getStudySize() {
            StudySizeDTO res = dicomClientService.sizeStudies();
            return ResponseEntity.ok(res);
    }
    @GetMapping("/diagnoses")
    public ResponseEntity<?> findAllDiagnose() {
            List<DiagnoseDTO> res = diagnoseService.getAllDiagnose();
            return ResponseEntity.ok(res);
    }
    @GetMapping("/diagnoses/{studyUID}")
    public ResponseEntity<?> getDiagnoseByStudyUID(@PathVariable String studyUID) {
        Diagnose res = diagnoseService.getDiagnoseByStudyId(studyUID);
        return ResponseEntity.ok(res);
    }
}