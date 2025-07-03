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

//    private List<Attributes> parseDicomJsonToAttributes(String jsonResponse) {
//        List<Attributes> attributesList = new ArrayList<>();
//
//        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse))) {
//            JsonArray jsonArray = jsonReader.readArray();
//
//            for (int i = 0; i < jsonArray.size(); i++) {
//                JsonObject studyJson = jsonArray.getJsonObject(i);
//                Attributes attributes = parseJsonObjectToAttributes(studyJson);
//                attributesList.add(attributes);
//            }
//        } catch (Exception e) {
//            System.err.println("Error parsing DICOM JSON: " + e.getMessage());
//            System.err.println("Full response body: " + jsonResponse);
//            throw new RuntimeException("Error parsing DICOM JSON: " + e.getMessage());
//        }
//
//        return attributesList;
//    }
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

//    private Attributes parseJsonObjectToAttributes(JsonObject jsonObject) {
//        Attributes attributes = new Attributes();
//
//        for (String tag : jsonObject.keySet()) {
//            try {
//                int tagInt = Integer.parseInt(tag, 16);
//                JsonObject tagObject = jsonObject.getJsonObject(tag);
//
//                if (tagObject.containsKey("Value")) {
//                    Object value = extractValueFromJson(tagObject.get("Value"));
//                    if (value != null) {
//                        setAttributeValue(attributes, tagInt, value);
//                    }
//                }
//            } catch (NumberFormatException e) {
//                System.err.println("Invalid DICOM tag format: " + tag);
//            }
//        }
//
//        return attributes;
//    }


    private Object extractValueFromJson(javax.json.JsonValue jsonValue) {
        if (jsonValue.getValueType() == javax.json.JsonValue.ValueType.ARRAY) {
            JsonArray array = (JsonArray) jsonValue;
            if (array.size() == 1) {
                return extractSingleValue(array.get(0));
            } else {
                List<Object> values = new ArrayList<>();
                for (javax.json.JsonValue value : array) {
                    values.add(extractSingleValue(value));
                }
                return values.toArray();
            }
        } else {
            return extractSingleValue(jsonValue);
        }
    }


    private Object extractSingleValue(javax.json.JsonValue jsonValue) {
        switch (jsonValue.getValueType()) {
            case STRING:
                return ((javax.json.JsonString) jsonValue).getString();
            case NUMBER:
                return ((javax.json.JsonNumber) jsonValue).numberValue();
            case OBJECT:
                // Handle PersonName objects
                JsonObject nameObject = (JsonObject) jsonValue;
                if (nameObject.containsKey("Alphabetic")) {
                    return nameObject.getString("Alphabetic");
                }
                return nameObject.toString();
            default:
                return jsonValue.toString();
        }
    }

    /**
     * Set giá trị cho DICOM attribute với VR phù hợp
     */
    private void setAttributeValue(Attributes attributes, int tag, Object value) {
        VR vr = attributes.getVR(tag);
        if (vr == null) {
            // Default VR cho một số tag phổ biến
            switch (tag) {
                case Tag.StudyInstanceUID:
                case Tag.SeriesInstanceUID:
                case Tag.SOPInstanceUID:
                    vr = VR.UI;
                    break;
                case Tag.StudyDate:
                case Tag.SeriesDate:
                case Tag.InstanceCreationDate:
                    vr = VR.DA;
                    break;
                case Tag.StudyTime:
                case Tag.SeriesTime:
                case Tag.InstanceCreationTime:
                    vr = VR.TM;
                    break;
                case Tag.PatientName:
                case Tag.StudyDescription:
                case Tag.SeriesDescription:
                    vr = VR.LO;
                    break;
                case Tag.Modality:
                    vr = VR.CS;
                    break;
                default:
                    vr = VR.LO; // Default to Long String
            }
        }

        if (value instanceof String) {
            attributes.setString(tag, vr, (String) value);
        } else if (value instanceof Number) {
            if (vr == VR.IS) {
                attributes.setString(tag, vr, value.toString());
            } else {
                attributes.setInt(tag, vr, ((Number) value).intValue());
            }
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            if (array.length > 0 && array[0] instanceof String) {
                String[] stringArray = Arrays.copyOf(array, array.length, String[].class);
                attributes.setString(tag, vr, stringArray);
            } else if (array.length > 0 && array[0] instanceof Number) {
                int[] intArray = new int[array.length];
                for (int i = 0; i < array.length; i++) {
                    intArray[i] = ((Number) array[i]).intValue();
                }
                attributes.setInt(tag, vr, intArray);
            }
        }
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
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                try (JsonReader jsonReader = Json.createReader(new StringReader(response.getBody()))) {
////                    JsonObject studyJson = jsonReader.readObject();
////                    return parseJsonObjectToAttributes(studyJson);
//                    JsonArray studies = jsonReader.readArray();
//                    if (studies.isEmpty()) {
//                        throw new RuntimeException("No study found with UID: " + studyInstanceUID);
//                    }
//                    JsonObject studyJson = studies.getJsonObject(0);
//                    return parseJsonObjectToAttributes(studyJson);
//                }
//            } else {
//                throw new RuntimeException("Failed to fetch study: " + response.getStatusCode());
//            }
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

//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return parseDicomJsonToAttributes(response.getBody());
//            } else if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getBody() == null || response.getBody().isBlank()) {
//                // Không có instance nào, trả về list rỗng
//                return Collections.emptyList();
//            }
//            else {
//                throw new RuntimeException("Failed to fetch instances: " + response.getStatusCode());
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching instances: " + e.getMessage());
        }
    }

    private Instance parseDicomFile(Attributes attrs) throws IOException {
        // try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            // Attributes attrs = dis.readDataset(-1, -1);

            Instance instance = new Instance();
            instance.setInstanceNumber(attrs.getString(Tag.InstanceNumber));
            instance.setStudyInstanceUID(attrs.getString(Tag.StudyInstanceUID));
            instance.setSeriesInstanceUID(attrs.getString(Tag.SeriesInstanceUID));
            instance.setSopInstanceUID(attrs.getString(Tag.SOPInstanceUID));
            instance.setSopClassUID(attrs.getString(Tag.SOPClassUID));
            instance.setInstanceCreationDate(attrs.getString(Tag.InstanceCreationDate));

            return instance;
        // }
    }
//    private Instance mapToInstance(Attributes instanceMap) {
//        Instance instance = new Instance();
//        instance.setInstanceNumber(instanceMap.getString(0x00200013));
////        instance.setInstanceNumber(extractStringValue(instanceMap, "00200013"));
////        instance.setReferencedSopInstanceUID(extractStringValue(instanceMap, "00080018"));
////        instance.setStudyInstanceUID(extractStringValue(instanceMap, "0020000D"));
////        instance.setSeriesInstanceUID(extractStringValue(instanceMap, "0020000E"));
////        instance.setSopClassUID(extractStringValue(instanceMap, "00080016"));
////        instance.setInstanceCreationDate(extractStringValue(instanceMap, "00080012"));
////        instance.setInstanceCreationTime(extractDateValue(instanceMap, "00080013"));
////        instance.setPixelData(extractStringValue(instanceMap, "7FE00010"));
////        instance.setSopInstanceUID(extractStringValue(instanceMap, "00080018"));
//        return instance;
//    }
////    private Instance mapToInstance(Map<String, Object> instanceMap) {
////        Instance instance = new Instance();
////        instance.setInstanceNumber(extractStringValue(instanceMap, "00200013"));
////        instance.setReferencedSopInstanceUID(extractStringValue(instanceMap, "00080018"));
////        instance.setStudyInstanceUID(extractStringValue(instanceMap, "0020000D"));
////        instance.setSeriesInstanceUID(extractStringValue(instanceMap, "0020000E"));
////        instance.setSopClassUID(extractStringValue(instanceMap, "00080016"));
////        instance.setInstanceCreationDate(extractStringValue(instanceMap, "00080012"));
////        instance.setInstanceCreationTime(extractDateValue(instanceMap, "00080013"));
////        instance.setPixelData(extractStringValue(instanceMap, "7FE00010"));
////        instance.setSopInstanceUID(extractStringValue(instanceMap, "00080018"));
////        return instance;
////    }
//    private String extractStringValue(Map<String, Object> objectMap, String tag) {
//        Map<String, Object> attribute = (Map<String, Object>) objectMap.get(tag);
//        if (attribute == null) {
//            return null;
//        }
//        List<String> values = (List<String>) attribute.get("Value");
//        if (values != null && !values.isEmpty()) {
//            return values.get(0);
//        }
//        return null;
//    }
//
//    private String extractNameValue(Map<String, Object> objectMap, String tag) {
//        Map<String, Object> attribute = (Map<String, Object>) objectMap.get(tag);
//        if (attribute == null) {
//            return null;
//        }
//        List<Map<String, String>> values = (List<Map<String, String>>) attribute.get("Value");
//        if (values != null && !values.isEmpty()) {
//            return values.get(0).get("Alphabetic");
//        }
//        return null;
//    }
//
//    private Date extractDateValue(Map<String, Object> objectMap, String tag) {
//        String dateStr = extractStringValue(objectMap, tag);
//        if (dateStr == null) {
//            return null;
//        }
//        try {
//            return new SimpleDateFormat("yyyyMMdd").parse(dateStr);
//        } catch (ParseException e) {
////            logger.error("Error parsing date: " + dateStr, e);
//            return null;
//        }
//    }
//
//    private Integer extractIntValue(Map<String, Object> objectMap, String tag) {
//        String intStr = extractStringValue(objectMap, tag);
//        if (intStr == null) {
//            return null;
//        }
//        try {
//            return Integer.parseInt(intStr);
//        } catch (NumberFormatException e) {
////            logger.error("Error parsing integer: " + intStr, e);
//            return null;
//        }
//    }
    ///
    ///



    //    public List<Attributes> fetchPatients() throws Exception {
//        String url = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs/studies";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", "application/dicom+json");
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//        );
//
//        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//            ObjectMapper mapper = new ObjectMapper();
//
//            // ✅ parse JSON string directly
//            List<Map<String, Object>> dicomJsonList = mapper.readValue(
//                    response.getBody(), // String
//                    new TypeReference<List<Map<String, Object>>>() {}
//            );
//
//            List<Attributes> result = new ArrayList<>();
//            for (Map<String, Object> studyJson : dicomJsonList) {
//                Attributes attrs = DicomJsonConverter.convertDicomJsonToAttributes(studyJson);
//                result.add(attrs);
//            }
//
//            return result;
//        } else {
//            throw new RuntimeException("Failed to fetch DICOM studies: " + response.getStatusCode());
//        }
//
//    }

}