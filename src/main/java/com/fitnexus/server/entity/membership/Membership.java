package com.fitnexus.server.entity.membership;

import com.fitnexus.server.entity.classes.OnlineClassMembership;
import com.fitnexus.server.entity.classes.physical.PhysicalClassMembership;
import com.fitnexus.server.entity.gym.GymMembership;
import com.fitnexus.server.entity.membership.corporate.Corporate;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.enums.MembershipStatus;
import com.fitnexus.server.enums.MembershipType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private MembershipType type;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    //days
    private long duration;
    private int slotCount;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal price;

    private double discount;

    //only for gym day pass
    private String eligibleDays;

    @CreationTimestamp
    private LocalDateTime startDateTime;

    @OneToOne(mappedBy = "membership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private GymMembership gymMembership;

    //activation code - new CR
    private String activationCode;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "membership", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassMembership> physicalClassMemberships;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "membership", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OnlineClassMembership> onlineClassMemberships;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PublicUserMembership> publicUserMemberships;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_id")
    private Corporate corporate;
}
