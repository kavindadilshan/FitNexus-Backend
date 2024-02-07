package com.fitnexus.server.dto.businessprofile;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaymentSummaryDTO {
    private LocalDateTime date;
    private long paymentReference;
    private long businessProfileId;
    private String beneficiary;
    private BigDecimal amount;
    private BigDecimal balance;
    private String paymentMethod;
    @ToString.Exclude
    private String paymentProof;
}
