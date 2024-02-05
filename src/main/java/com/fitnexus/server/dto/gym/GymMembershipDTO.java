package com.fitnexus.server.dto.gym;

import com.fitnexus.server.dto.businessprofile.BusinessProfileLocationDTO;
import com.fitnexus.server.dto.membership.DayPassDTO;
import com.fitnexus.server.dto.membership.MembershipDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class GymMembershipDTO extends MembershipDTO {
    private long gymId;
    private String gymName;
    private String gymUniqueName;
    private String gymDescription;
    private double gymRating;
    private long gymRatingCount;
    private String gymImage;
    private BusinessProfileLocationDTO location;
    private double distance;
    private DayPassDTO dayPassDTO;
}
