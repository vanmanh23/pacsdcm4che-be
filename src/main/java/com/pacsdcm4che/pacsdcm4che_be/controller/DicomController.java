package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.entity.Instance;
import com.pacsdcm4che.pacsdcm4che_be.entity.Patient;
import com.pacsdcm4che.pacsdcm4che_be.entity.Series;
import com.pacsdcm4che.pacsdcm4che_be.entity.Study;
import com.pacsdcm4che.pacsdcm4che_be.service.DicomClientService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dicom")
@CrossOrigin(origins = "*")
public class DicomController {

    @Autowired
    private DicomClientService dicomClientService;

    @GetMapping("/studies/{studyInstanceUID}/tags")
    public ResponseEntity<Map<String, Object>> getStudyTags(@PathVariable String studyInstanceUID) {
        try {
            List<Attributes> instancesList = dicomClientService.getStudyByUID(studyInstanceUID);

            Attributes attributes = instancesList.stream()
                    .findFirst()
                    .orElse(null);

            if (attributes == null) {
                return ResponseEntity.notFound().build();
            }


            Map<String, Object> tags = new HashMap<>();
            
            // Đọc các DICOM tags quan trọng
            tags.put("StudyInstanceUID", attributes.getString(Tag.StudyInstanceUID));
            tags.put("StudyID", attributes.getString(Tag.StudyID));
            tags.put("StudyDate", attributes.getDate(Tag.StudyDate));
            tags.put("StudyTime", attributes.getString(Tag.StudyTime));// nhơ getdate moi dung
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
            
            return ResponseEntity.ok(tags);
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
}