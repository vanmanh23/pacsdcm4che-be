package com.pacsdcm4che.pacsdcm4che_be.dtos;

import lombok.Data;
import java.util.Date;

@Data
public class InstanceDTO {
    private String referencedSopInstanceUID;
    private String instanceNumber;
    private String sopClassUID;
    private String sopInstanceUID;
    private String studyInstanceUID;
    private String seriesInstanceUID;
    private String pixelData;
    private Date instanceCreationDate;
    private Date instanceCreationTime;

    public String getReferencedSopInstanceUID() {
        return referencedSopInstanceUID;
    }

    public void setReferencedSopInstanceUID(String referencedSopInstanceUID) {
        this.referencedSopInstanceUID = referencedSopInstanceUID;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String sopClassUID) {
        this.sopClassUID = sopClassUID;
    }

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getPixelData() {
        return pixelData;
    }

    public void setPixelData(String pixelData) {
        this.pixelData = pixelData;
    }

    public Date getInstanceCreationDate() {
        return instanceCreationDate;
    }

    public void setInstanceCreationDate(Date instanceCreationDate) {
        this.instanceCreationDate = instanceCreationDate;
    }

    public Date getInstanceCreationTime() {
        return instanceCreationTime;
    }

    public void setInstanceCreationTime(Date instanceCreationTime) {
        this.instanceCreationTime = instanceCreationTime;
    }
}
