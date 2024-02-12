package com.fitnexus.server.dto.classsession;

import com.fitnexus.server.enums.SessionEnrollStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SessionEnrollPublicDTO {

    private long enrollmentId;
    private long businessProfileId;
    private String businessProfileName;
    private long classId;
    private String className;
    private String classType;
    private String coachName;
    private long classSessionId;
    private String classSessionName;
    private LocalDateTime enrollDateTime;
    private LocalDateTime sessionDateTime;
    private double paidAmount;
    private SessionEnrollStatus paymentStatus;
}
