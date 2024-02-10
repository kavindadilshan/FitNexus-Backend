package com.fitnexus.server.dto.publicuser;

import com.fitnexus.server.dto.common.PinVerifyDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class PublicUserRegisterDTO extends PublicUserDTO {
    private PinVerifyDTO otpDetails;
    private String password;
}
