package com.fitnexus.server.dto.classes;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClassForTrainerDTO {

    private long classId;
    private String className;
    private String classType;
    private long numberOfSessions;
}
