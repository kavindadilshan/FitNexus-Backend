package com.fitnexus.server.dto.classsession;

import com.fitnexus.server.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SessionCreateDTO {

    private int maxJoiners;
    private long duration;
    private String description;
    private double price;
    private Gender gender;
    private List<LocalDateTime> dateTimeList;
    private long classId;
    private long trainerId;
    private String language;
    private long locationId;
    private boolean allowCashPayment;
    private List<Long> facilityIdList;
}
