package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.entity.*;
import com.pacsdcm4che.pacsdcm4che_be.service.DiagnoseService;
import com.pacsdcm4che.pacsdcm4che_be.service.DicomClientService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
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
    public ResponseEntity<String> uploadDicom(@RequestParam("file") MultipartFile[] file) {
        ArrayList<String> listRespone = new ArrayList<String>();
        try {
            for (MultipartFile multipartFile : file) {
                String respon = dicomClientService.uploadDicomFile(multipartFile);
                listRespone.add(respon);
            }
            return ResponseEntity.ok(listRespone.toString());
//            return ResponseEntity.ok("update dicom file successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/studies/{studyInstanceUID}/tags")
    public ResponseEntity<Study> getStudyTags(@PathVariable String studyInstanceUID) {
        try {
            List<Attributes> instancesList = dicomClientService.getStudyByUID(studyInstanceUID);

            Attributes attributes = instancesList.stream()
                    .findFirst()
                    .orElse(null);

            if (attributes == null) {
                return ResponseEntity.notFound().build();
            }

            Study study = new Study();
            Map<String, Object> tags = new HashMap<>();
            
            // Đọc các DICOM tags quan trọng
            tags.put("StudyInstanceUID", attributes.getString(Tag.StudyInstanceUID));
            tags.put("StudyID", attributes.getString(Tag.StudyID));
            tags.put("StudyDate", attributes.getDate(Tag.StudyDate));
            tags.put("StudyTime", attributes.getDate(Tag.StudyTime));// nhơ getdate moi dung
            tags.put("AccessionNumber", attributes.getString(Tag.AccessionNumber));

            tags.put("StudyDescription", attributes.getString(Tag.StudyDescription));
            tags.put("ReferringPhysicianName", attributes.getString(Tag.ReferringPhysicianName));
            tags.put("Modality", attributes.getString(Tag.Modality));
            tags.put("NumberOfSeries", attributes.getInt(Tag.NumberOfSeriesRelatedInstances, 0));
            
            // Patient information
            tags.put("PatientName", attributes.getString(Tag.PatientName));
            tags.put("PatientID", attributes.getString(Tag.PatientID));
            tags.put("PatientBirthDate", attributes.getDate(Tag.PatientBirthDate));
            tags.put("PatientSex", attributes.getString(Tag.PatientSex));
            if (attributes.contains(Tag.StudyDescription)) {
                System.out.println("Tag exists!");
            }
            //
            study.setStudyID(attributes.getString(Tag.StudyID));
            study.setStudyDate(attributes.getDate(Tag.StudyDate));
            study.setStudyTime(attributes.getDate(Tag.StudyTime));
            study.setStudyDescription(attributes.getString(Tag.StudyDescription));
            study.setModality(attributes.getString(Tag.ModalitiesInStudy));
            study.setStudyInstanceUID(attributes.getString(Tag.InstanceCreatorUID));
            study.setAccessionNumber(attributes.getString(Tag.AccessionNumber));
            study.setReferringPhysicianName(attributes.getString(Tag.ReferringPhysicianName));
            study.setNumberOfInstances(attributes.getInt(Tag.NumberOfStudyRelatedInstances, 0));
            study.setNumberOfSeries(attributes.getInt(Tag.NumberOfSeriesRelatedInstances, 0));
            study.setStudyInstanceUID(attributes.getString(Tag.StudyInstanceUID));
//            return ResponseEntity.ok(tags);
            return ResponseEntity.ok(study);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/tags")
    public ResponseEntity<Map<String, Object>> getSeriesTags(
            @PathVariable String studyInstanceUID,
            @PathVariable String seriesInstanceUID) {
        try {
            List<Attributes> seriesList = dicomClientService.getSeriesByStudyUID(studyInstanceUID);
            Attributes targetSeries = seriesList.stream()
                    .filter(series -> seriesInstanceUID.equals(series.getString(Tag.SeriesInstanceUID)))
                    .findFirst()
                    .orElse(null);
            
            if (targetSeries == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> tags = new HashMap<>();
            tags.put("SeriesInstanceUID", targetSeries.getString(Tag.SeriesInstanceUID));
            tags.put("SeriesNumber", targetSeries.getString(Tag.SeriesNumber));
            tags.put("Modality", targetSeries.getString(Tag.Modality));
            tags.put("SeriesDescription", targetSeries.getString(Tag.SeriesDescription));
            tags.put("SeriesDate", targetSeries.getString(Tag.SeriesDate));
            tags.put("SeriesTime", targetSeries.getString(Tag.SeriesTime));// chú ý ngày tháng get date
//            tags.put("NumberOfInstances", targetSeries.getInt(Tag.NumberOfInstances, 0));
            tags.put("BodyPartExamined", targetSeries.getString(Tag.BodyPartExamined));
            tags.put("ProtocolName", targetSeries.getString(Tag.ProtocolName));
            
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances/{instanceUID}/tags")
    public ResponseEntity<Map<String, Object>> getInstanceTags(
            @PathVariable String studyInstanceUID,
            @PathVariable String seriesInstanceUID,
            @PathVariable String instanceUID) {
        try {
            List<Attributes> instancesList = dicomClientService.getInstancesBySeriesUID(studyInstanceUID, seriesInstanceUID);
            Attributes targetInstance = instancesList.stream()
                    .filter(instance -> instanceUID.equals(instance.getString(Tag.SOPInstanceUID)))
                    .findFirst()
                    .orElse(null);

            if (targetInstance == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> tags = new HashMap<>();
            tags.put("SOPInstanceUID", targetInstance.getString(Tag.SOPInstanceUID));
            tags.put("InstanceNumber", targetInstance.getString(Tag.InstanceNumber));
            tags.put("SOPClassUID", targetInstance.getString(Tag.SOPClassUID));
            tags.put("InstanceCreationDate", targetInstance.getString(Tag.InstanceCreationDate));
            tags.put("InstanceCreationTime", targetInstance.getString(Tag.InstanceCreationTime));
            tags.put("ImageType", targetInstance.getString(Tag.ImageType));
            tags.put("ImageComments", targetInstance.getString(Tag.ImageComments));

            // Image specific tags
            tags.put("Rows", targetInstance.getInt(Tag.Rows, 0));
            tags.put("Columns", targetInstance.getInt(Tag.Columns, 0));
            tags.put("BitsAllocated", targetInstance.getInt(Tag.BitsAllocated, 0));
            tags.put("BitsStored", targetInstance.getInt(Tag.BitsStored, 0));
            tags.put("HighBit", targetInstance.getInt(Tag.HighBit, 0));
            tags.put("PixelRepresentation", targetInstance.getInt(Tag.PixelRepresentation, 0));
            tags.put("SamplesPerPixel", targetInstance.getInt(Tag.SamplesPerPixel, 0));
            tags.put("PhotometricInterpretation", targetInstance.getString(Tag.PhotometricInterpretation));

            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
@GetMapping("/studies/{studyInstanceUID}/series/{seriesInstanceUID}/instances/{instanceUID}/images")
public ResponseEntity<byte[]> getInstanceImages(
        @PathVariable String studyInstanceUID,
        @PathVariable String seriesInstanceUID,
        @PathVariable String instanceUID) {
    try {
        ResponseEntity<byte[]> respon = dicomClientService.getInstancesImage(studyInstanceUID, seriesInstanceUID, instanceUID);
        return respon;
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients () {
        try {
            List<Attributes> patientsList = dicomClientService.getPatients();
            Attributes targetPatients = patientsList.stream()
                    .findFirst()
                    .orElse(null);
            if (targetPatients == null) {
                return ResponseEntity.notFound().build();
            }
            Patient patient = new Patient();
            patient.setPatientID(targetPatients.getString(Tag.PatientID));
            patient.setPatientName(targetPatients.getString(Tag.PatientName));
            patient.setSex(targetPatients.getString(Tag.PatientSex));
            patient.setPatientBirthDate(targetPatients.getDate(Tag.PatientBirthDate));

            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/patients/{PatientUid}")
    public ResponseEntity<?> getPatientsByUID (@PathVariable String PatientUid) {
        try {
            List<Attributes> patientsList = dicomClientService.getPatients();
            Attributes targetPatients = patientsList.stream()
                    .filter(patient -> PatientUid.equals(patient.getString(Tag.PatientID)))
                    .findFirst()
                    .orElse(null);
            if (targetPatients == null) {
                return ResponseEntity.notFound().build();
            }
            Patient patient = new Patient();
            patient.setPatientID(targetPatients.getString(Tag.PatientID));
            patient.setPatientName(targetPatients.getString(Tag.PatientName));
            patient.setSex(targetPatients.getString(Tag.PatientSex));
            patient.setPatientBirthDate(targetPatients.getDate(Tag.PatientBirthDate));

            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/diagnose")
    public ResponseEntity<Diagnose> createDiagnose(@RequestBody Diagnose diagnose) {
        Diagnose createdDiagnose = diagnoseService.saveDiagnose(diagnose);
        return new ResponseEntity<>(createdDiagnose, HttpStatus.CREATED);
    }

}