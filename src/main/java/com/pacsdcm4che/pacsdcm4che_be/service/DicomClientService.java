package com.pacsdcm4che.pacsdcm4che_be.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacsdcm4che.pacsdcm4che_be.entity.Instance;
import com.pacsdcm4che.pacsdcm4che_be.entity.Patient;
import com.pacsdcm4che.pacsdcm4che_be.entity.Series;
import com.pacsdcm4che.pacsdcm4che_be.entity.Study;
import com.pacsdcm4che.pacsdcm4che_be.repository.StudyRepository;
import org.dcm4che3.data.Attributes;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.dcm4che3.json.JSONReader;
import com.fasterxml.jackson.core.JsonFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

@Service
public class DicomClientService {
    //    private static final Logger logger = LoggerFactory.getLogger(DicomClientService.class);
    @Autowired
    private final RestTemplate restTemplate;
    @Autowired
    private StudyRepository studyRepository;
    private static final String STOW_RS_URL = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs";

    public DicomClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public List<Attributes> getStudiesWithDicomTags() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseDicomJsonToAttributes(response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch studies: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching studies with DICOM tags: " + e.getMessage());
        }
    }

    private List<Attributes> parseDicomJsonToAttributes(String jsonResponse) {
        List<Attributes> attributesList = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Parse chuỗi JSON thành List<Map<String, Object>>
            List<Map<String, Object>> dicomJsonList = mapper.readValue(
                    jsonResponse,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> dicomJson : dicomJsonList) {
                Attributes attributes = DicomJsonConverter.convertDicomJsonToAttributes(dicomJson);
                attributesList.add(attributes);
            }

        } catch (Exception e) {
            System.err.println("Error parsing DICOM JSON: " + e.getMessage());
            System.err.println("Full response body: " + jsonResponse);
            throw new RuntimeException("Error parsing DICOM JSON", e);
        }
        return attributesList;
    }


//    public Attributes getStudyByUID(String studyInstanceUID) {
    public List<Attributes> getStudyByUID(String studyInstanceUID) {
        try {
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Accept", "multipart/related; type=application/dicom");
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies?StudyInstanceUID=" + studyInstanceUID,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null && !body.isBlank()) {
                try {
                    return parseDicomJsonToAttributes(body);
                } catch (Exception e) {
                    System.err.println("Error parsing DICOM JSON: " + e.getMessage());
                    System.err.println("Response body: " + body);
                    throw new RuntimeException("Error parsing DICOM JSON: " + e.getMessage());
                }
            } else if (response.getStatusCode() == HttpStatus.NO_CONTENT || body == null || body.isBlank()) {
                // Không có instance nào, trả về list rỗng
                return Collections.emptyList();
            } else {
                throw new RuntimeException("Failed to fetch instances: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching study by UID: " + e.getMessage());
        }
    }

    public List<Attributes> getSeriesByStudyUID(String studyInstanceUID) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies/" + studyInstanceUID + "/series",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseDicomJsonToAttributes(response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch series: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching series by study UID: " + e.getMessage());
        }
    }

    public List<Attributes> getInstancesBySeriesUID(String studyInstanceUID, String seriesInstanceUID) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/instances",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null && !body.isBlank()) {
                try {
                    return parseDicomJsonToAttributes(body);
                } catch (Exception e) {
                    System.err.println("Error parsing DICOM JSON: " + e.getMessage());
                    System.err.println("Response body: " + body);
                    throw new RuntimeException("Error parsing DICOM JSON: " + e.getMessage());
                }
            } else if (response.getStatusCode() == HttpStatus.NO_CONTENT || body == null || body.isBlank()) {
                // Không có instance nào, trả về list rỗng
                return Collections.emptyList();
            } else {
                throw new RuntimeException("Failed to fetch instances: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching instances: " + e.getMessage());
        }
    }

}