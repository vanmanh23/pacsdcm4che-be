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
}
