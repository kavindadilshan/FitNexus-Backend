package com.fitnexus.server.dto.membership;

import com.fitnexus.server.enums.BusinessProfilePaymentModel;
import com.fitnexus.server.enums.MembershipStatus;
import com.fitnexus.server.enums.MembershipType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MembershipDTO implements Comparable {
    private long membershipId;
    private long publicUserMembershipId;
    private MembershipType type;
    private String name;
    private String description;
    //days
    private long duration;
    private int slotCount;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private double discount;
    private long numberOfPurchase;
    private MembershipStatus status;
    private BusinessProfilePaymentModel paymentModel;
    private boolean allowCashPayment;
    private String businessName;
    private List<String> trainers;

    @Override
    public int compareTo(Object o) {
        if (this.status.equals(MembershipStatus.OPEN)) return 1;
        else return -1;
    }

    public MembershipDTO(long membershipId, String name, MembershipType type) {
        this.membershipId = membershipId;
        this.name = name;
        this.type = type;
    }
}
