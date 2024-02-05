package com.fitnexus.server.dto.membership;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DayPassForGymDTO extends MembershipDTO{
    private DayPassDTO dayPassDTO;
}

