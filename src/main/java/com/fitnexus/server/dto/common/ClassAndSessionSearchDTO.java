package com.fitnexus.server.dto.common;

import com.fitnexus.server.enums.ClassCategory;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ClassAndSessionSearchDTO {
    private String text;
    private ClassCategory category;
    private long classId;
    private long businessProfileId;
}
