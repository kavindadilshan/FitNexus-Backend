package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserDiscountsHistory;
import com.fitnexus.server.enums.SessionEnrollStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"public_user_id", "physical_session_id"})})
public class PhysicalSessionEnroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "physical_session_id")
    private PhysicalClassSession physicalClassSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PublicUser publicUser;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private PublicUserDiscountsHistory publicUserDiscountsHistory;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private SessionEnrollStatus status;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal paidAmount;

    @Column(unique = true)
    private String paymentId;

    private LocalDateTime paymentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by")
    private AuthUser collectedBy;

}
