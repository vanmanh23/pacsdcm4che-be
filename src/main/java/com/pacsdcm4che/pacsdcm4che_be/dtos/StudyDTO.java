package com.pacsdcm4che.pacsdcm4che_be.dtos;
import lombok.Data;

import java.util.Date;

@Data
public class StudyDTO {
    private String studyInstanceUID;
    private String studyID;
    private Date studyDate;
    private Date studyTime;
    private String accessionNumber;
    private String studyDescription;
    private String referringPhysicianName;
    private String modality;
    private Integer numberOfSeries;
    private Integer numberOfInstances;

    private String patientID;
    private String patientName;
    private String sex;
    private Date patientBirthDate;
}
