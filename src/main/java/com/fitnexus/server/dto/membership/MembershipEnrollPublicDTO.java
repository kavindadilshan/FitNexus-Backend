package com.fitnexus.server.dto.membership;

import com.fitnexus.server.enums.MembershipStatus;
import com.fitnexus.server.enums.MembershipType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MembershipEnrollPublicDTO {

    private long userMembershipId;
    private long businessProfileId;
    private String businessProfileName;
    private long membershipId;
    private String membershipName;
    private MembershipType membershipType;
    private List<String> classNames;
    private String gymName;
    private LocalDateTime enrollDateTime;
    private LocalDateTime expireDateTime;
    private BigDecimal paidAmount;
    private MembershipStatus paymentStatus;

    public MembershipEnrollPublicDTO(long userMembershipId, long businessProfileId, String businessProfileName,
                                     long membershipId, String membershipName, MembershipType membershipType,
                                     String gymName, LocalDateTime enrollDateTime,
                                     LocalDateTime expireDateTime, BigDecimal paidAmount) {
        this.userMembershipId = userMembershipId;
        this.businessProfileId = businessProfileId;
        this.businessProfileName = businessProfileName;
        this.membershipId = membershipId;
        this.membershipName = membershipName;
        this.membershipType = membershipType;
        this.gymName = gymName;
        this.enrollDateTime = enrollDateTime;
        this.expireDateTime = expireDateTime;
        this.paidAmount = paidAmount;
    }
}
