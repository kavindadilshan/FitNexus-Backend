package com.fitnexus.server.entity.businessprofile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class BusinessProfilePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_agreement_id")
    private BusinessAgreement businessAgreement;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @Digits(integer = 9, fraction = 2)
    private BigDecimal amount;

    @Lob
    private String description;

    private String paymentReport;
}
