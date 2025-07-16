package com.pacsdcm4che.pacsdcm4che_be.dtos;

import lombok.Data;
import java.util.Date;

@Data
public class PatientDTO {
    private String patientID;
    private String patientName;
    private String sex;
    private Date patientBirthDate;
    @Override
    public String toString() {
        return "Patient{" +
                "patientID=" + patientID +
                ", patientName=" + patientName +
                ", sex=" + sex +
                ", patientBirthDate=" + patientBirthDate +
                '}';
    }
}