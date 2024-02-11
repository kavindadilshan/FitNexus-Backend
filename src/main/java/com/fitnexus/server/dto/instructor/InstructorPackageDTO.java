package com.fitnexus.server.dto.instructor;

import com.fitnexus.server.enums.InstructorPackageStatus;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class InstructorPackageDTO {

    private long instructorPackageId;
    private String packageName;
    private String packageDescription;
    private long instructorId;
    private String instructorName;
    //    private long packageTypeId;
//    private String packageTypeName;
    private int timePeriod;
    private InstructorPackageStatus status;
    private long businessProfileId;
    private String businessProfileName;
    private double price;
    private double ratings;
    private String createdBy;
    private String createdDate;
    private String modifiedBy;
    private String modifiedDate;
    private long numberOfEnrollments;
    private List<InstructorPackageEnrollmentDTO> enrollments;

}
