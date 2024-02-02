package com.fitnexus.server.service.impl;

import com.fitnexus.server.dto.common.PinVerifyDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.entity.auth.MobileOtp;
import com.fitnexus.server.repository.auth.MobileOTPRepository;
import com.fitnexus.server.service.OTPService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.sms.SmsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;



@Component
@Slf4j
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

    private final MobileOTPRepository mobileOTPRepository;
    private final SmsHandler smsHandler;

    /**
     * @param mobile the mobile number which will get the random otp
     */
    @Override
    public void sendOtpAndSaveIt(String mobile) {
        // validate mobile for SMS
        Optional<MobileOtp> optionalMobileOtp = mobileOTPRepository.findById(mobile);
        if (optionalMobileOtp.isPresent() && optionalMobileOtp.get().getDateTime() != null
                && optionalMobileOtp.get().getDateTime().plusSeconds(30).isAfter(LocalDateTime.now()))
            throw new CustomServiceException("Please wait 30 seconds from the last OTP");
        String otp = CustomGenerator.generateOTP();
        MobileOtp mobileOtp = new MobileOtp();
        mobileOtp.setMobile(mobile);
        mobileOtp.setOtp(otp);
        mobileOTPRepository.save(mobileOtp);

        // send sms
        smsHandler.sendMessages(Collections.singletonList(mobile), "Please use the OTP " + otp + " to log in to Fitzky App");
        log.info("OTP send to " + mobile + " : " + true);
    }



}
