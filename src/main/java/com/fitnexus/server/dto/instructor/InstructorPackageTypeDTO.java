package com.fitnexus.server.dto.instructor;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InstructorPackageTypeDTO {

    private long packageTypeId;
    private String name;
    private int timePeriod;
}
