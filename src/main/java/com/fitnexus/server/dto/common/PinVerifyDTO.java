package com.fitnexus.server.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@Getter
@Setter
@ToString
public class PinVerifyDTO {
    private String mobile;
    private String otp;

    public PinVerifyDTO(String mobile, String otp) {
        this.mobile = mobile;
        this.otp = otp;
    }

}
