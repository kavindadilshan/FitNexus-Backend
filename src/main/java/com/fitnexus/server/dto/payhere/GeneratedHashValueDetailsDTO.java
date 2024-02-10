package com.fitnexus.server.dto.payhere;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GeneratedHashValueDetailsDTO {

    private String orderId;
    private String amount;
    private String hash;
    private String currency;
    private String notifyUrl;
    private String accessToken;
    private String customerToken;

}
