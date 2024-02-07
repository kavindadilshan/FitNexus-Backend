package com.fitnexus.server.dto.classes;

import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.DiscountCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ClassRevenueSummaryDTO {

    private String publicUserName;
    private String className;
    private ClassCategory category;
    private LocalDateTime sessionDateTime;
    private BigDecimal sessionFee;
    private BigDecimal paidAmount;
    private DiscountCategory discountCode;
    private BigDecimal studioRev;
    private BigDecimal fitzkyRev;
    private BigDecimal stripeFee;
}
