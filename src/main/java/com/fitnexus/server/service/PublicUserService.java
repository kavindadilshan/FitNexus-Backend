package com.fitnexus.server.service;

import com.fitnexus.server.dto.admin.FrontendEventDTO;
import com.fitnexus.server.dto.classsession.SessionZoomDetails;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.NotificationTokenDTO;
import com.fitnexus.server.dto.common.OTPRequestDTO;
import com.fitnexus.server.dto.common.PinVerifyDTO;
import com.fitnexus.server.dto.payhere.GeneratedHashValueDetailsDTO;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.entity.classes.ClassSession;
import com.fitnexus.server.entity.classes.ClassSessionEnroll;
import com.fitnexus.server.entity.classes.packages.PublicUserPackageSubscription;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.entity.publicuser.PublicUserNotification;
import com.fitnexus.server.enums.AuthType;
import com.fitnexus.server.enums.EventType;
import com.fitnexus.server.enums.UserDeviceTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.model.PaymentIntent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public interface PublicUserService {

    void requestRegisterOtp(OTPRequestDTO otpRequestDTO);


}
