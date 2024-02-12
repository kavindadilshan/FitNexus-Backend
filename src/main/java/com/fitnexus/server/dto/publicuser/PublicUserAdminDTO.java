package com.fitnexus.server.dto.publicuser;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PublicUserAdminDTO {

    private PublicUserCommonDTO publicUserCommonDTO;
    private long totalNumberOfClassEnrollments;
    private long totalNumberOfInstructorEnrolls;
}
