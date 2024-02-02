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

    /**
     * @param pinVerifyDTO the request which contains the number and the received otp
     * @throws CustomServiceException if the otp is not matching or if an another error occurred
     */
    @Override
    public void verifyOtp(PinVerifyDTO pinVerifyDTO) throws CustomServiceException {

        Optional<MobileOtp> optionalOtp = mobileOTPRepository.findById(pinVerifyDTO.getMobile());
        if (!optionalOtp.isPresent()) {
            throw new CustomServiceException(404, "No OTP for the number");
        }
        MobileOtp mobileOtp = optionalOtp.get();

        long hours = ChronoUnit.HOURS.between(mobileOtp.getDateTime(), LocalDateTime.now());
        if (hours >= 24) {
            throw new CustomServiceException(403, "OTP is expired, please re-send");
        }

        if (mobileOtp.getOtp() != null && mobileOtp.getOtp().equals(pinVerifyDTO.getOtp())) {
            log.info("OTP Matches : " + pinVerifyDTO);
            return;
        }

        // this is only used for testing stage
//        if (mobileOtp.getOtp() != null && pinVerifyDTO.getOtp().equals("1234")) {
//            log.info("Default OTP Matches : " + "1234");
//            return;
//        }
        throw new CustomServiceException(401, "Incorrect OTP");

    }


}
