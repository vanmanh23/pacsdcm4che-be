package com.pacsdcm4che.pacsdcm4che_be.dtos;

import lombok.Data;
<<<<<<< HEAD
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


=======
import java.util.Date;

>>>>>>> df72f9ae2dffacf61b7b12492d1966965f0b9850
@Data
public class PatientDTO {
    private String patientID;
    private String patientName;
    private String sex;
    private Date patientBirthDate;
<<<<<<< HEAD

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

=======
>>>>>>> df72f9ae2dffacf61b7b12492d1966965f0b9850
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