package com.pacsdcm4che.pacsdcm4che_be.service;


import com.pacsdcm4che.pacsdcm4che_be.entity.Patient;
import com.pacsdcm4che.pacsdcm4che_be.entity.Study;
import com.pacsdcm4che.pacsdcm4che_be.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DicomClientService {
    private static final Logger logger = LoggerFactory.getLogger(DicomClientService.class);
    @Autowired
    private final RestTemplate restTemplate;
    @Autowired
    private StudyRepository studyRepository;
    private static final String STOW_RS_URL = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs";

    public DicomClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//        public String uploadDicomFile(MultipartFile dicomFile) throws IOException {
//
//            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//
//                String boundary = "----Boundary" + System.currentTimeMillis();
//                HttpPost postRequest = new HttpPost(STOW_RS_URL + "/studies");
//
//                postRequest.setHeader("Content-Type",
//                        "multipart/related; type=\"application/dicom\"; boundary=" + boundary);
//
//                InputStream inputStream = dicomFile.getInputStream();
//
//                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//                builder.setBoundary(boundary);
//                builder.setMimeSubtype("related");
//
//                builder.addBinaryBody(
//                        "dicomfile",
//                        inputStream,
//                        ContentType.create("application/dicom"),
//                        dicomFile.getOriginalFilename()
//                );
//
//                postRequest.setEntity(builder.build());
//                System.out.println("Request: " + postRequest.toString());
//                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
//                    int statusCode = response.getStatusLine().getStatusCode();
//                    String responseBody = EntityUtils.toString(response.getEntity());
//                    return "Status: " + statusCode + ", Response: " + responseBody;
//                }
//            }
//        }
    public List<Object> getPatients() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    STOW_RS_URL + "/patients",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> responseBody = response.getBody();
            if(responseBody == null){
                return Collections.emptyList();
            }
            return responseBody.stream()
                .map(this::mapToPatient)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching patients: " + e.getMessage());
    }
        }

    private Object mapToPatient(Map<String, Object> patientMap) {
        Patient patient = new Patient();
        patient.setPatientID(extractStringValue(patientMap, "00100020"));
        patient.setPatientName(extractNameValue(patientMap, "00100010"));
        patient.setIssuerOfPatientID(extractStringValue(patientMap, "00100021"));
        patient.setPatientAge(extractStringValue(patientMap, "00101010"));

        return patient;
    }

    public List<Study> getStudies() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/dicom+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                STOW_RS_URL + "/studies",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> responseBody = response.getBody();
        if (responseBody == null) {
            return Collections.emptyList();
        }

        return responseBody.stream()
                .map(this::mapToStudy)
                .collect(Collectors.toList());
        }

    private Study mapToStudy(Map<String, Object> studyMap) {
        Study study = new Study();
        study.setStudyInstanceUID(extractStringValue(studyMap, "0020000D"));
        study.setStudyID(extractStringValue(studyMap, "00200010"));
        study.setStudyDate(extractDateValue(studyMap, "00080020"));
        study.setStudyTime(extractStringValue(studyMap, "00080030"));
        study.setAccessionNumber(extractStringValue(studyMap, "00080050"));
        study.setStudyDescription(extractStringValue(studyMap, "00081030"));
        study.setReferringPhysicianName(extractNameValue(studyMap, "00080090"));
        study.setModality(extractStringValue(studyMap, "00080061"));
        study.setNumberOfSeries(extractIntValue(studyMap, "00201206"));
        study.setNumberOfInstances(extractIntValue(studyMap, "00201208"));
        return study;
    }

    private String extractStringValue(Map<String, Object> objectMap, String tag) {
        Map<String, Object> attribute = (Map<String, Object>) objectMap.get(tag);
        System.out.println("attribute ppppp: " + attribute);
        if (attribute == null) {
            return null;
        }
        List<String> values = (List<String>) attribute.get("Value");
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    private String extractNameValue(Map<String, Object> objectMap, String tag) {
        Map<String, Object> attribute = (Map<String, Object>) objectMap.get(tag);
        if (attribute == null) {
            return null;
        }
        List<Map<String, String>> values = (List<Map<String, String>>) attribute.get("Value");
        if (values != null && !values.isEmpty()) {
            return values.get(0).get("Alphabetic");
        }
        return null;
    }
    
    private Date extractDateValue(Map<String, Object> objectMap, String tag) {
        String dateStr = extractStringValue(objectMap, tag);
        if (dateStr == null) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyyMMdd").parse(dateStr);
        } catch (ParseException e) {
            logger.error("Error parsing date: " + dateStr, e);
            return null;
        }
    }

    private Integer extractIntValue(Map<String, Object> objectMap, String tag) {
        String intStr = extractStringValue(objectMap, tag);
        if (intStr == null) {
            return null;
        }
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            logger.error("Error parsing integer: " + intStr, e);
            return null;
        }
    }
}