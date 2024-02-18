package com.fitnexus.server.dto.classsession;

import lombok.*;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ClassSessionBookDTO {
    private long sessionId;
    private long userId;
    private String paymentMethodId;
    private long discountId;
}
