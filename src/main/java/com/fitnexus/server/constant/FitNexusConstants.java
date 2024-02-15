package com.fitnexus.server.constant;

import java.math.BigDecimal;


public class FitNexusConstants {

    public static final class ErrorConstants {
        public static final String SERVICE_ERROR = "Service Error, please contact support!";
        public static final String INVALID_OLD_PASS = "Invalid old password";
        public static final String SMS_PASSCODE = "Q2V5ZW50cmFUZWNobm9sb2dpZXNDb2xsYWJvcmF0ZVdpdGhMaW9uVG91cnNAMTIzNDU=";
        public static final String SMS_CODE_NOT_ALLOWED = "Invalid SMS passcode!";
        public static final String USER_NOT_ACTIVATED = "User not activated!";
        public static final String FORBIDDEN_RESOURCE = "You are not authorized to access this resource!";
        public static final String BUSINESS_PROFILE_EXPIRED = "Business profile expired!";
        public static final int TOO_MANY_REQUESTS = 429;
    }

    public static final class NotFoundConstants {
        public static final String NO_USER_FOUND = "User not found";
        public static final String NO_ADMIN_FOUND = "Admin not found";
        public static final String NO_MANAGER_FOUND = "Business profile manager not found";
        public static final String NO_CLASS_FOUND = "Class not found";
        public static final String NO_SESSION_FOUND = "Class session not found";
        public static final String NO_COACH_FOUND = "Coach not found";
        public static final String NO_INSTRUCTOR_FOUND = "Instructor not found";
        public static final String NO_INSTRUCTOR_PACKAGE_TYPE_FOUND = "Instructor package type not found";
        public static final String NO_INSTRUCTOR_PACKAGE_FOUND = "Instructor package not found";
        public static final String NO_TRAINER_FOUND = "Trainer not found";
        public static final String NO_PUBLIC_USER_FOUND = "Public user not found";
        public static final String NO_CLASS_TYPE_FOUND = "Class type not found";
        public static final String NO_CARD_FOUND = "Card not found";
        public static final String NO_CLASS_SUBSCRIPTION_FOUND = "Class subscription not found";
        public static final String NO_PACKAGE_FOUND = "Package not found";
        public static final String NO_BUSINESS_PROFILE_FOUND = "Business profile not found";
        public static final String NO_TRAINER_SUBSCRIPTION_FOUND = "Trainer subscription not found";
        public static final String NO_INSTR_SUBSCRIPTION_FOUND = "Instructor subscription not found";
        public static final String NO_PAYMENT_MODEL_FOUND = "Payment model not found";
        public static final String NO_USER_ROLE_FOUND = "User role not found";
        public static final String NO_LOCATION_FOUND = "Business profile location not found";
        public static final String NO_GYM_FOUND = "Gym not found";
        public static final String NO_MEMBERSHIP_FOUND = "Membership not found";
        public static final String NO_MEMBERSHIP_PURCHASE_FOUND = "Membership purchase not found";
        public static final String NO_CORPORATE_FOUND = "Corporate not found";
        public static final String INVALID_AUTH_PROVIDER = "Invalid auth provider";
        public static final String INVALID_REFERRAL_CODE = "Invalid referral code";

    }

    public static final class DuplicatedConstants {
        public static final String MOBILE_ALREADY_EXISTS = "You can not use this mobile number!";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String SOCIAL_ALREADY_EXISTS = "Social media account already exists";
        public static final String BUSINESS_PROFILE_ALREADY_EXISTS = "Business profile already exists";
        public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
        public static final String VERIFICATION_ALREADY_EXISTS = "Verification no already exists";
        public static final String INSTRUCTOR_PACKAGE_ALREADY_EXISTS = "Instructor package already exists";
        public static final String INSTRUCTOR_PACKAGE_TYPE_ALREADY_EXISTS = "Instructor package type already exists";
        public static final String CLASS_TYPE_ALREADY_EXISTS = "Class type already exists";
        public static final String CLASS_NAME_ALREADY_EXISTS = "Class name already exists";
        public static final String SESSION_ALREADY_EXISTS = "Session already exists";
        public static final String MEMBERSHIP_ALREADY_EXISTS = "We found another membership for the given name";
    }

    public static final class NotPresentedConstants {
        public static final String MOBILE_REQUIRED = "Mobile number is required";

    }

    public static final class PatternConstants {
        public static final String DATE_TIME_RESPONSE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        public static final String DATE_PATTERN = "yyyy-MM-dd";
        public static final String TIME_PATTERN = "HH:mm:ss";
        public static final String TIME_ZONE = "GMT";
        public static final String REGEX = "[^A-Za-z0-9]";
    }


}
