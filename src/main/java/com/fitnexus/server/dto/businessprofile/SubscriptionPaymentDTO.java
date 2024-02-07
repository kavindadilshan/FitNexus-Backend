package com.fitnexus.server.dto.businessprofile;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class SubscriptionPaymentDTO {
    private long paymentId;
    private String businessProfileName;
    private String registrationNumber;
    private long agreementId;
    private LocalDateTime dateTime;
    private BigDecimal paidAmount;
    private LocalDateTime expiringDateTime;
}
