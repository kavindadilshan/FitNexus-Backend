package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.membership.Membership;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PhysicalClassMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean allowCashPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physical_class_id")
    private PhysicalClass physicalClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id")
    private Membership membership;

    public PhysicalClassMembership(PhysicalClass physicalClass, Membership membership, boolean allowCashPayment) {
        this.physicalClass = physicalClass;
        this.membership = membership;
        this.allowCashPayment = allowCashPayment;
    }
}
