package com.pacsdcm4che.pacsdcm4che_be.service;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


@Service
public class DicomClientService {
    @Autowired
    private final RestTemplate restTemplate;
    private static final String STOW_RS_URL = "http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs";

    public DicomClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

        public String uploadDicomFile(MultipartFile dicomFile) throws Exception {
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
                    return "Status: " + statusCode + ", Response: " + responseBody;
                }
            }
        }
        public List<Object> getpatients() {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    STOW_RS_URL + "/patients",
                    List.class
            );
            return response.getBody();
        }
        public List<Object> getStudies() {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    STOW_RS_URL + "/studies",
                    List.class
            );
            return response.getBody();
        }
        public List<Object> getInstances() {
            ResponseEntity<List> reponse = restTemplate.getForEntity(
                    STOW_RS_URL + "/instances",
                    List.class
            );
            return reponse.getBody();
        }
        public List<Object> getSeries() {
            ResponseEntity<List> reponse = restTemplate.getForEntity(
                    STOW_RS_URL + "/series",
                    List.class
//                    "PACS"
            );
            return reponse.getBody();
        }

}