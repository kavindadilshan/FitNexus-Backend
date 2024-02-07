package com.fitnexus.server.dto.membership;

import com.fitnexus.server.enums.MembershipType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MembershipRevenueSummaryDTO {

    private String publicUserName;
    private String membershipName;
    private MembershipType type;
    private long duration;
    private BigDecimal listedPrice;
    private BigDecimal discountedPrice;
    private double discount;
    private BigDecimal paidAmount;
    private BigDecimal studioRev;
    private BigDecimal fitzkyRev;
    private BigDecimal stripeFee;
    private String gymName;
    private List<String> classNames;
}
