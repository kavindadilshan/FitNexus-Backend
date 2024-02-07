package com.fitnexus.server.entity.businessprofile;

import com.fitnexus.server.entity.publicuser.PublicUserDiscountsHistory;
import com.fitnexus.server.enums.BusinessProfileRevenueType;
import com.fitnexus.server.enums.IPGType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
public class BusinessProfileRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @CreationTimestamp
    private LocalDateTime dateTime;

    private String description;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal fitzkyAmount;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal profileAmount;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal ipgAmount;

    @Enumerated(EnumType.STRING)
    private IPGType ipgType;

    @Enumerated(EnumType.STRING)
    private BusinessProfileRevenueType type;
    private String typeId;
    @Column(unique = true)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private PublicUserDiscountsHistory publicUserDiscountsHistory;

    public BusinessProfileRevenue(String description, @Digits(integer = 9, fraction = 2) BigDecimal fitzkyAmount,
                                  @Digits(integer = 9, fraction = 2) BigDecimal profileAmount,
                                  @Digits(integer = 9, fraction = 2) BigDecimal ipgAmount,
                                  BusinessProfileRevenueType type, String typeId, BusinessProfile businessProfile) {
        this.description = description;
        this.fitzkyAmount = fitzkyAmount;
        this.profileAmount = profileAmount;
        this.ipgAmount = ipgAmount;
        this.type = type;
        this.typeId = typeId;
        this.businessProfile = businessProfile;
    }
}
