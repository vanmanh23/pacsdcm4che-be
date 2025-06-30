package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.entity.Study;
import com.pacsdcm4che.pacsdcm4che_be.service.DicomClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dicomweb")
public class DicomController {
    private final DicomClientService dicomClientService;
    public DicomController(DicomClientService dicomClientService) {
        this.dicomClientService = dicomClientService;
    }

//    @GetMapping("/client/studies")
//    public ResponseEntity<String> getStudiesFromClient() {
//        return ResponseEntity.ok(dicomClientService.getStudies());
//    }
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadDicom(@RequestParam("file") MultipartFile file) {
//        try {
//            String result = dicomClientService.uploadDicomFile(file);
//            System.out.println(result);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
//        }
//    }
    @GetMapping("/patients")
    public List<Object> getpatients() {
        return dicomClientService.getPatients();
    }

    @GetMapping("/studies")
    public List<Study> getStudies() {
        return dicomClientService.getStudies();
    }
//    @GetMapping("/studiesmetadata")
//    public ResponseEntity<String> getStudiesMetadata(@RequestParam String studyInstanceUID) {
//        return ResponseEntity.ok(dicomClientService.getStudyMetadata(studyInstanceUID));
//    }
}