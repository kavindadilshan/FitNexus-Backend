package com.fitnexus.server.dto.payhere;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PreApproveResponseDTO {

    private String orderId;
    private String amount;
    private String hash;
    private String currency;
    private String notifyUrl;


}
