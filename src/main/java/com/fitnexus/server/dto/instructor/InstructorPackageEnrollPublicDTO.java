package com.fitnexus.server.dto.instructor;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class InstructorPackageEnrollPublicDTO {

    private long enrollmentId;
    private long businessProfileId;
    private String businessProfileName;
    private long instructorId;
    private String instructorName;
    private long instructorPackageId;
    private String instructorPackageName;
    private LocalDateTime enrollDateTime;
    private LocalDateTime expireDateTime;
    private double paidAmount;
}
