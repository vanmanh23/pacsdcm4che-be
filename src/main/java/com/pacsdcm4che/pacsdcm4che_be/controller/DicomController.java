package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.service.DicomClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/dicomweb")
public class DicomController {
    private final DicomClientService dicomClientService;
    public DicomController(DicomClientService dicomClientService) {
        this.dicomClientService = dicomClientService;
    }

    @GetMapping("/client/studies")
    public List<Object> getStudiesFromClient() {
        return dicomClientService.getStudies();
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDicom(@RequestParam("file") MultipartFile file) {
        try {
            String result = dicomClientService.uploadDicomFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
    @GetMapping("/patients")
    public List<Object> getpatients() {
        return dicomClientService.getpatients();
    }
    @GetMapping("/series")
    public List<Object> getSeries() {
        return dicomClientService.getSeries();
    }
    @GetMapping("/instances")
    public List<Object> getInstances() {
        return dicomClientService.getInstances();
    }

}