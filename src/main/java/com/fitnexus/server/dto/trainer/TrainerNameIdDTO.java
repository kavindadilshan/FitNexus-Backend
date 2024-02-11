package com.fitnexus.server.dto.trainer;

import com.fitnexus.server.enums.CoachStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TrainerNameIdDTO {
    private long id;
    private String name;
    private CoachStatus status;
}
