package com.fitnexus.server.dto.classes;

import com.fitnexus.server.dto.common.RateDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ClassRateDTO extends RateDTO {
    private long classId;
}
