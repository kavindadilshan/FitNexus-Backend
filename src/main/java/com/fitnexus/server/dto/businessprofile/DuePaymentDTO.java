package com.fitnexus.server.dto.businessprofile;

import com.fitnexus.server.enums.BusinessProfilePaymentModel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DuePaymentDTO {
    private long businessProfileId;
    private String businessProfileName;
    private String businessProfileRegNumber;
    private long agreementNo;
    private BusinessProfilePaymentModel paymentModel;
    private BigDecimal dueAmount;
    private LocalDateTime lastPaidDateTime;
}
