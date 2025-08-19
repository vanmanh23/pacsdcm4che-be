package com.pacsdcm4che.pacsdcm4che_be.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacsdcm4che.pacsdcm4che_be.dtos.*;
import com.pacsdcm4che.pacsdcm4che_be.entity.Diagnose;
import com.pacsdcm4che.pacsdcm4che_be.repository.DiagnoseRepository;
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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import com.fasterxml.jackson.core.JsonGenerator;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

@Service
public class DicomClientService {

    @Autowired
    private final RestTemplate restTemplate;

    private static final String STOW_RS_URL = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs";

    @Autowired
    private DiagnoseRepository diagnoseRepository;

    public DicomClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

        public Map<String, String>  uploadDicomFile(MultipartFile dicomFile) throws IOException {

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
                        Map<String,String> studySeriesInstanceIdsFromXmlResponse =extractStudySeriesInstanceIdsFromXmlResponse(responseBody);
                        if (studySeriesInstanceIdsFromXmlResponse == null) {
                            throw new IOException("No studyInstanceUID found in response");
                        }
                        if (diagnoseRepository.existsByStudyId(studySeriesInstanceIdsFromXmlResponse.get("studyInstanceUID"))) {
                            System.out.println("StudyInstanceUID already exists");
                        }else {
                            Diagnose diagnose = new Diagnose();
                            diagnose.setStudyId(studySeriesInstanceIdsFromXmlResponse.get("studyInstanceUID"));
                            diagnoseRepository.save(diagnose);
                        }

                        //save studyInstanceUID vào bảng chuẩn đoán
//                        Diagnose diagnose = new Diagnose();
//                        diagnose.setStudyId(studySeriesInstanceIdsFromXmlResponse.get("studyInstanceUID"));
//                        if (!diagnoseRepository.existsByStudyId(studySeriesInstanceIdsFromXmlResponse.get("studyInstanceUID"))) {
//                            diagnoseRepository.save(diagnose);
//                        }
                        return studySeriesInstanceIdsFromXmlResponse;
                    } else {
                        throw new IOException("Upload failed. Status: " + statusCode + ". Response: " + responseBody);
                    }
                }
            }
        }
private Map<String, String> extractStudySeriesInstanceIdsFromXmlResponse(String responseBody) {
    try {
        // Regex to find studies/<UID>/series/<UID>/instances/<UID>
        Pattern pattern = Pattern.compile("rs/studies/([0-9.]+)/series/([0-9.]+)/instances/([0-9.]+)");
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            Map<String, String> ids = new HashMap<>();
            ids.put("studyInstanceUID", matcher.group(1));
            ids.put("seriesInstanceUID", matcher.group(2));
            ids.put("instanceUID", matcher.group(3));
            return ids;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
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


    public List<StudyDTO> getStudyByUID() {
        try {
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Accept", "multipart/related; type=application/dicom");
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies?limit=1000",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null && !body.isBlank()) {
                try {
                    List<Attributes> attributesList = parseDicomJsonToAttributes(body);
                    List<StudyDTO> studyDTOList = new ArrayList<>();
                    for (Attributes attributes : attributesList) {
                        StudyDTO studyDTO = new StudyDTO();
                        studyDTO.setStudyID(attributes.getString(Tag.StudyID));
                        studyDTO.setStudyDate(attributes.getDate(Tag.StudyDate));
                        studyDTO.setStudyTime(attributes.getDate(Tag.StudyTime));
                        studyDTO.setStudyDescription(attributes.getString(Tag.StudyDescription));
                        studyDTO.setModality(attributes.getString(Tag.ModalitiesInStudy));
                        studyDTO.setStudyInstanceUID(attributes.getString(Tag.StudyInstanceUID));
                        studyDTO.setAccessionNumber(attributes.getString(Tag.AccessionNumber));
                        studyDTO.setReferringPhysicianName(attributes.getString(Tag.ReferringPhysicianName));
                        studyDTO.setNumberOfInstances(attributes.getInt(Tag.NumberOfStudyRelatedInstances, 0));
                        studyDTO.setNumberOfSeries(attributes.getInt(Tag.NumberOfStudyRelatedSeries, 0));

                        studyDTO.setPatientID(attributes.getString(Tag.PatientID));
                        studyDTO.setPatientName(attributes.getString(Tag.PatientName));
                        studyDTO.setSex(attributes.getString(Tag.PatientSex));
                        studyDTO.setPatientBirthDate(attributes.getDate(Tag.PatientBirthDate));

                        studyDTOList.add(studyDTO);
                    }

                    return studyDTOList;


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

    public List<SeriesDTO> getSeriesByStudyUID(String studyInstanceUID) {
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
                List<Attributes> attributesList = parseDicomJsonToAttributes(response.getBody());
                List<SeriesDTO> seriesDTOList = new ArrayList<>();
                for (Attributes attributes : attributesList) {
                    SeriesDTO seriesDTO = new SeriesDTO();
                    seriesDTO.setSeriesInstanceUID(attributes.getString(Tag.SeriesInstanceUID));
                    seriesDTO.setSeriesDescription(attributes.getString(Tag.SeriesDescription));
                    seriesDTO.setModality(attributes.getString(Tag.Modality));
                    seriesDTO.setNumberOfInstances(attributes.getInt(Tag.NumberOfSeriesRelatedInstances, 0));
                    seriesDTO.setSeriesDate(attributes.getDate(Tag.SeriesDate));
                    seriesDTO.setSeriesNumber(attributes.getString(Tag.SeriesNumber));
                    seriesDTO.setSeriesTime(attributes.getDate(Tag.SeriesTime));
                    seriesDTO.setStudyInstanceUID(studyInstanceUID);

                    seriesDTOList.add(seriesDTO);
                }
                return seriesDTOList;
            } else {
                throw new RuntimeException("Failed to fetch series: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching series by study UID: " + e.getMessage());
        }
    }

    public List<InstanceDTO> getInstancesBySeriesUidAndStudyUid(String studyInstanceUID, String seriesInstanceUID) {
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
                    List<Attributes> attributesList = parseDicomJsonToAttributes(response.getBody());
                    List<InstanceDTO> instanceDTOList = new ArrayList<>();
                    for (Attributes attributes : attributesList) {
                        InstanceDTO instanceDTO = new InstanceDTO();
                        instanceDTO.setReferencedSopInstanceUID(attributes.getString(Tag.ReferencedSOPInstanceUID));
                        instanceDTO.setInstanceNumber(attributes.getString(Tag.InstanceNumber));
                        instanceDTO.setSopClassUID(attributes.getString(Tag.SOPClassUID));
                        instanceDTO.setSopInstanceUID(attributes.getString(Tag.SOPInstanceUID));
                        instanceDTO.setStudyInstanceUID(studyInstanceUID);
                        instanceDTO.setSeriesInstanceUID(seriesInstanceUID);
                        instanceDTO.setPixelData(attributes.getString(Tag.PixelData));
                        instanceDTO.setInstanceCreationDate(attributes.getDate(Tag.InstanceCreationDate));
                        instanceDTO.setInstanceCreationTime(attributes.getDate(Tag.InstanceCreationTime));

                        instanceDTOList.add(instanceDTO);
                    }
                    return instanceDTOList;

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


public ResponseEntity<List<String>> getInstancesImage(String studyInstanceUID, String seriesInstanceUID , String instanceUID) {
    try {
        HttpHeaders imageHeaders = new HttpHeaders();
//        imageHeaders.set("Accept", "image/jpeg");
        imageHeaders.set("Accept", "application/json");
        HttpEntity<Void> imageEntity = new HttpEntity<>(imageHeaders);

        ResponseEntity<String> imageResponse = restTemplate.exchange(
                STOW_RS_URL + "/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/instances/" + instanceUID + "/metadata",
//                STOW_RS_URL + "/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/instances/" + instanceUID + "/rendered",
                HttpMethod.GET,
                imageEntity,
                String.class
//                byte[].class
        );
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(imageResponse.getBody());
        JsonNode firstObj = root.get(0);

        int numberOfFrames = 1; // mặc định single-frame
        if (firstObj.has("00280008")) {
            numberOfFrames = firstObj.get("00280008").get("Value").get(0).asInt();
        }

        // Tạo danh sách URL frames
        List<String> frameUrls = new ArrayList<>();
        for (int i = 1; i <= numberOfFrames; i++) {
            String url = STOW_RS_URL + "/studies/" + studyInstanceUID
                    + "/series/" + seriesInstanceUID
                    + "/instances/" + instanceUID
                    + "/frames/" + i + "/rendered";
            frameUrls.add(url);
        }

        // Trả list URL về dạng JSON
        return ResponseEntity.ok(frameUrls);

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error fetching instances: " + e.getMessage());
    }
}

    public List<PatientDTO> getPatients() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/dicom+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/patients",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            System.out.println("----------------------------: ");
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Attributes> attributesList = parseDicomJsonToAttributes(response.getBody());
                List<PatientDTO> patientDTOList = new ArrayList<>();
                for (Attributes attributes : attributesList) {
                    PatientDTO patientDTO = new PatientDTO();
                    patientDTO.setPatientID(attributes.getString(Tag.PatientID));
                    patientDTO.setPatientName(attributes.getString(Tag.PatientName));
                    patientDTO.setSex(attributes.getString(Tag.PatientSex));
                    patientDTO.setPatientBirthDate(attributes.getDate(Tag.PatientBirthDate));

                    patientDTOList.add(patientDTO);
                }
                return patientDTOList;
            } else if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getBody() == null || response.getBody().isBlank()) {
                return Collections.emptyList();
            } else {
                throw new RuntimeException("Failed to fetch studies: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching studies with DICOM tags: " + e.getMessage());
        }
    }
//
public List<InstanceDTO> searchForInstances() {
    try {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/dicom+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                STOW_RS_URL + "/instances",
                HttpMethod.GET,
                entity,
                String.class
        );
        String body = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK && body != null && !body.isBlank()) {
            try {
                List<Attributes> attributesList = parseDicomJsonToAttributes(response.getBody());
                List<InstanceDTO> instanceDTOList = new ArrayList<>();
                for (Attributes attributes : attributesList) {
                    InstanceDTO instanceDTO = new InstanceDTO();
                    instanceDTO.setReferencedSopInstanceUID(attributes.getString(Tag.ReferencedSOPInstanceUID));
                    instanceDTO.setInstanceNumber(attributes.getString(Tag.InstanceNumber));
                    instanceDTO.setSopClassUID(attributes.getString(Tag.SOPClassUID));
                    instanceDTO.setSopInstanceUID(attributes.getString(Tag.SOPInstanceUID));
                    instanceDTO.setStudyInstanceUID(attributes.getString(Tag.StudyInstanceUID));
                    instanceDTO.setSeriesInstanceUID(attributes.getString(Tag.SeriesInstanceUID));
                    instanceDTO.setPixelData(attributes.getString(Tag.PixelData));
                    instanceDTO.setInstanceCreationDate(attributes.getDate(Tag.InstanceCreationDate));
                    instanceDTO.setInstanceCreationTime(attributes.getDate(Tag.InstanceCreationTime));

                    instanceDTOList.add(instanceDTO);
                }
                return instanceDTOList;

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
        throw new RuntimeException("Error fetching instances with DICOM tags: " + e.getMessage());
    }
}
    public StudyCountDTO countStudies() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies/count",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK) {
                StudyCountDTO countDTO = new StudyCountDTO();
                JSONObject json = new JSONObject(body);
                countDTO.setCount(json.getInt("count"));
                return countDTO;
            }  else {
                throw new RuntimeException("Failed to fetch studies count: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching studies count with DICOM tags: " + e.getMessage());
        }
    }
    public StudySizeDTO sizeStudies() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    STOW_RS_URL + "/studies/size",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String body = response.getBody();
            System.out.println("========body: " + body);
            if (response.getStatusCode() == HttpStatus.OK) {
                StudySizeDTO studySizeDTO = new StudySizeDTO();
                JSONObject json = new JSONObject(body);
                Double sizeMB = json.getDouble("size") / (1000 * 1000);
                studySizeDTO.setSize((int) Math.round(sizeMB));
                return studySizeDTO;
            }  else {
                throw new RuntimeException("Failed to fetch studies size: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching studies size with DICOM tags: " + e.getMessage());
        }
    }
}