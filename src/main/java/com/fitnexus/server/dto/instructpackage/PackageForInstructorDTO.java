package com.fitnexus.server.dto.instructpackage;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PackageForInstructorDTO {

    private long packageId;
    private String packageName;
    private int timePeriod;
    private String description;
    private double fee;
}
