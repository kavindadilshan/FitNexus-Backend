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

        public static final String NO_MANAGER_FOUND = "Business profile manager not found";

        public static final String NO_BUSINESS_PROFILE_FOUND = "Business profile not found";

        public static final String NO_LOCATION_FOUND = "Business profile location not found";


    }

    public static final class DuplicatedConstants {
        public static final String BUSINESS_PROFILE_ALREADY_EXISTS = "Business profile already exists";
       }


}
