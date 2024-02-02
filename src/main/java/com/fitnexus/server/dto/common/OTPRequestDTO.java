package com.fitnexus.server.dto.common;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class OTPRequestDTO {
    private String smsSecret;
    private String mobile;
}
