package com.pacsdcm4che.pacsdcm4che_be.dtos;

import lombok.Data;
import java.util.Date;

@Data
public class SeriesDTO {
    private String seriesInstanceUID;
    private String seriesDescription;
    private String seriesNumber;
    private String modality;
    private Integer numberOfInstances;
    private Date seriesDate;
    private Date seriesTime;
    private String studyInstanceUID;
}
