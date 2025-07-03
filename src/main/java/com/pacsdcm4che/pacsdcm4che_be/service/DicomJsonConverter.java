package com.pacsdcm4che.pacsdcm4che_be.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

import java.util.List;
import java.util.Map;

public class DicomJsonConverter {

    public static Attributes convertDicomJsonToAttributes(Map<String, Object> dicomJson) {
        Attributes attributes = new Attributes();

        for (Map.Entry<String, Object> entry : dicomJson.entrySet()) {
            String tagHex = entry.getKey(); // VD: "00100010"
            int tag = Integer.parseInt(tagHex, 16); // chuyển sang int

            Map<String, Object> tagContent = (Map<String, Object>) entry.getValue();
            String vrCode = (String) tagContent.get("vr");
            VR vr = VR.valueOf(vrCode);

            Object valueObj = tagContent.get("Value");
            if (valueObj instanceof List<?> valueList && !valueList.isEmpty()) {
                Object firstValue = valueList.get(0);
                // Xử lý trường hợp như PatientName có cấu trúc { Alphabetic: "..." }
                if (firstValue instanceof Map<?, ?> nameMap) {
                    Object alphabetic = nameMap.get("Alphabetic");
                    if (alphabetic instanceof String name) {
                        attributes.setString(tag, vr, name);
                    }
                } else {
                    // Chuyển toàn bộ danh sách thành String[]
                    String[] stringArray = valueList.stream()
                            .map(Object::toString)
                            .toArray(String[]::new);

                    attributes.setString(tag, vr, stringArray);
                }
            }
        }

        return attributes;
    }
}
