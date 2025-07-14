package com.pacsdcm4che.pacsdcm4che_be.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacsdcm4che.pacsdcm4che_be.entity.Instance;
import com.pacsdcm4che.pacsdcm4che_be.entity.Patient;
import com.pacsdcm4che.pacsdcm4che_be.entity.Series;
import com.pacsdcm4che.pacsdcm4che_be.entity.Study;
import com.pacsdcm4che.pacsdcm4che_be.repository.StudyRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dcm4che3.data.Attributes;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

//import com.fasterxml.jackson.core.JsonGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.dcm4che3.json.JSONReader;
import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DicomClientService {
    //    private static final Logger logger = LoggerFactory.getLogger(DicomClientService.class);
    @Autowired
    private final RestTemplate restTemplate;
    @Autowired
    private StudyRepository studyRepository;
    private ObjectMapper objectMapper;
    private static final String STOW_RS_URL = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs";

    public DicomClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

        public String uploadDicomFile(MultipartFile dicomFile) throws IOException {

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

                String boundary = "----Boundary" + System.currentTimeMillis();
                HttpPost postRequest = new HttpPost(STOW_RS_URL + "/studies");

                postRequest.setHeader("Content-Type",
                        "multipart/related; type=\"application/dicom\"; boundary=" + boundary);
                InputStream inputStream = dicomFile.getInputStream();

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setBoundary(boundary);
                builder.setMimeSubtype("related");

                builder.addBinaryBody(
                        "dicomfile",
                        inputStream,
                        ContentType.create("application/dicom"),
                        dicomFile.getOriginalFilename()
                );
                postRequest.setEntity(builder.build());
                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());
                    if (statusCode >= 200 && statusCode < 300) {
                        // Trích xuất studyInstanceUID từ response nếu có
                        String studyId = extractStudyIdFromXmlResponse(responseBody);
                        return studyId != null ? studyId : "Uploaded but studyId not found.";
                    } else {
                        throw new IOException("Upload failed. Status: " + statusCode + ". Response: " + responseBody);
                    }
                }
            }
        }
        private String extractStudyIdFromXmlResponse(String responseBody) {
            try {
                // Regex để tìm đoạn studies/<UID>
                Pattern pattern = Pattern.compile("studies/([0-9.]+)");
                Matcher matcher = pattern.matcher(responseBody);

                if (matcher.find()) {
                    return matcher.group(1); // studyInstanceUID
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
public ResponseEntity<byte[]> getInstancesImage(String studyInstanceUID, String seriesInstanceUID , String instanceUID) {
    try {
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders.set("Accept", "image/jpeg");
        HttpEntity<Void> imageEntity = new HttpEntity<>(imageHeaders);

        ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                STOW_RS_URL + "/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/instances/" + instanceUID + "/rendered",
                HttpMethod.GET,
                imageEntity,
                byte[].class
        );
        System.out.println("response rrrrrr: "+imageResponse);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageResponse.getBody());

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error fetching instances: " + e.getMessage());
    }
}
    public List<Attributes> getPatients() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            List<Attributes> attributesList = new ArrayList<>();
            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/patients",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            System.out.println("----------------------------: ");
            for (Object item : parseDicomJsonToAttributes(response.getBody())) {
                System.out.println("444444444444444"+ item);
                attributesList.add((Attributes) item);
            }
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return parseDicomJsonToAttributes(response.getBody());
                return attributesList;
            } else {
                throw new RuntimeException("Failed to fetch studies: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching studies with DICOM tags: " + e.getMessage());
        }
    }
//    public void updateStudy(String studyInstanceUID, Study dto) throws Exception {
//        if (objectMapper == null) {
//            objectMapper = new ObjectMapper();
//        }
//        Attributes attrs = new Attributes();
//
//        attrs.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
//
//        if (dto.getStudyID() != null)
//            attrs.setString(Tag.StudyID, VR.SH, dto.getStudyID());
//
//        if (dto.getStudyDescription() != null)
//            attrs.setString(Tag.StudyDescription, VR.LO, dto.getStudyDescription());
//
//        if (dto.getAccessionNumber() != null)
//            attrs.setString(Tag.AccessionNumber, VR.SH, dto.getAccessionNumber());
//
//        if (dto.getReferringPhysicianName() != null)
//            attrs.setString(Tag.ReferringPhysicianName, VR.PN, dto.getReferringPhysicianName());
//
//        if (dto.getModality() != null)
//            attrs.setString(Tag.ModalitiesInStudy, VR.CS, dto.getModality());
//
//        if (dto.getNumberOfSeries() != null)
//            attrs.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, dto.getNumberOfSeries());
//
////        if (dto.getNumberOfInstances() != null)
//            attrs.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, dto.getNumberOfInstances());
//
//        if (dto.getStudyDate() != null) {
//            attrs.setDate(Tag.StudyDate, VR.DA, dto.getStudyDate());
//        }
//        if (dto.getStudyTime() != null) {
//            attrs.setDate(Tag.StudyTime, VR.TM, dto.getStudyTime());
//        }
//
//        if (dto.getPatientID() != null)
//            attrs.setString(Tag.PatientID, VR.LO, dto.getPatientID());
////        if (dto.getPatientName() != null)
////            attrs.setString(Tag.PatientName, VR.PN, dto.getPatientName());
////        if (dto.getPatientBirthDate() != null)
////            attrs.setDate(Tag.PatientBirthDate, VR.DA, dto.getPatientBirthDate());
////        if (dto.getPatientSex() != null)
////            attrs.setString(Tag.PatientSex, VR.CS, dto.getPatientSex());
//
//        // Convert to DICOM JSON
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        JsonGenerator gen = Json.createGenerator(baos);
//        JSONWriter writer = new JSONWriter(gen);
//        writer.write(attrs);
//        gen.close();
//
//        String dicomJsonArray = "[" + baos.toString(StandardCharsets.UTF_8) + "]";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("application/dicom+json"));
//
//        HttpEntity<String> entity = new HttpEntity<>(dicomJsonArray, headers);
//
//        String url = STOW_RS_URL + "/studies/" + studyInstanceUID;
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("Update failed: " + response.getStatusCode());
//        }
//    }
}