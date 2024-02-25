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

    public static final class DetailConstants {
        public static final String CLASS_SESSION_BOOKED = "Class session booking is successful";
        public static final String INSTRU_PACK_SUBSCRIBED = "Personal trainer subscription is successful";
        public static final String MEMBERSHIP_PURCHASED = "Membership purchase is successful";
        public static final String DAY_PASS_PURCHASED = "Single entry pass purchase is successful";
        public static final String CLASS_SESSION_BOOKED_DESC_SMS = "Your reservation for the {class} on {dateTime} is confirmed. You may log in to the Fitzky App on your Mobile/Tab or log in to public.fitzky.com through your laptop/computer (Chrome Recommended)  to join the Online Class.";
        public static final String CLASS_SESSION_BOOKED_DESC = "Your reservation for the Online Class {class} on {dateTime} is confirmed. Find your booking details in the Notifications section.";
        public static final String PHYSICAL_SESSION_BOOKED_DESC = "Your reservation for the fitness class conducted on {dateTime} at {class} is confirmed.";
        public static final String ONLINE_SESSION_BOOKED_DESC = "Your reservation for the Online class conducted on {dateTime} at {class} is confirmed.";

        public static final String PHYSICAL_SESSION_CASH_BOOKED_DESC = "Your reservation for the fitness class {class} on {dateTime} is confirmed. Please do the payment to the coach on the class day to proceed.";

        public static final String MEMBERSHIP_PURCHASED_DESC = "Hi {firstName}, Your purchase of '{the membership}' at {gym/Class} is confirmed.";
        public static final String DAY_PASS_DESC = "Hi {firstName}, Your purchase of '{the dayPass}' at {gym/Class} is confirmed.";
        public static final String INSTRU_PACK_SUBSCRIBED_DESC = "Hi {firstName}, You have successfully subscribed to {instructor}'s {day} day(s) personal training package. ";

        public static final String CLASS_SESSION_BOOKED_FOR_COACH = "New Enrolment";
        public static final String CLASS_SESSION_BOOKED_DESC_FOR_COACH = "Hi {firstName}, You have a new reservation for your {class}";

        public static final String CLASS_SESSION_FAILED = "Failed Class session booking ";
        public static final String PHYSICAL_SESSION_FAILED = "Failed Fitness session booking ";
        public static final String INSTRU_PACK_FAILED = "Failed subscribing Instructor";
        public static final String MEMBERSHIP_FAILED = "Failed purchasing membership";
        public static final String CLASS_SESSION_FAIL_DESC = "Your purchase was unsuccessful due to an issue with your card. Please contact your bank. ";
        public static final String PHYSICAL_SESSION_FAIL_DESC = "Your purchase was unsuccessful due to an issue with your card. Please contact your bank. ";
        public static final String INSTRU_PACK_FAIL_DESC = "Your purchase was unsuccessful due to an issue with your card. Please contact your bank. ";
        public static final String MEMBERSHIP_FAILED_DESC = "Your purchase was unsuccessful due to an issue with your card. Please contact your bank. ";
        public static final int ENROLLED_CHECK_MINUTES = 5;
        public static final int RATE_DECIMAL_PLACES = 1;

        public static final String CLASS_SESSION_ASSIGNED = "Class session has been assigned";
        public static final String CLASS_SESSION_ASSIGNED_DESC = "Hi {firstName}, The session of {className} scheduled on {dateTime} is assigned to you";
        public static final String CLASS_SESSION_CANCELED = "Class session has been canceled";
        public static final String CLASS_SESSION_CANCELED_DESC = "Hi {firstName}, The session of {class} scheduled on {dateTime}  has been cancelled.";
        public static final String CLASS_SESSION_RESCHEDULED = "{className} has been rescheduled";
        public static final String CLASS_SESSION_RESCHEDULED_DESC = "Hi {firstName}, The session of {className} that’s scheduled on {oldDateTime} has been rescheduled for {newDateTime}";

        public static final String PHYSICAL_CLASS_SESSION_ASSIGNED = "Fitness class session has been assigned";
        public static final String PHYSICAL_CLASS_SESSION_ASSIGNED_DESC = "Hi {firstName}, The session of {className} scheduled on {dateTime} is assigned to you";
        public static final String PHYSICAL_CLASS_SESSION_LIST_ASSIGNED_DESC = "Hi {firstName}, You have been assigned to {session numbers} sessions of {class name} from {start date} to {end date}";
        public static final String ONLINE_CLASS_SESSION_LIST_ASSIGNED_DESC = "Hi {firstName}, You have been assigned to {session numbers} sessions of {class name} from {start date} to {end date}";
        public static final String PHYSICAL_CLASS_SESSION_CANCELED = "Fitness Class session has been canceled";
        public static final String PHYSICAL_CLASS_SESSION_CANCELED_DESC = "Hi {firstName}, The {class} scheduled on {dateTime} has been cancelled";
        public static final String PHYSICAL_CLASS_SESSION_RESCHEDULED = "Fitness class ({className}) has been rescheduled";
        public static final String PHYSICAL_CLASS_SESSION_RESCHEDULED_DESC = "Hi {firstName}, The {className} which is scheduled on {oldDateTime} has been rescheduled to {newDateTime}";
        public static final String TIME_ZONE_HEADER = "timeZone";

        public static final String SESSION_START_BEFORE_TITLE = "Class reminder.";
        public static final String ONLINE_SESSION_START_BEFORE_MESSAGE = "Hi {firstName}, Your assigned {className} is starting in {minutes} minutes. Please start the session for the enrolled participants to join video";
        public static final String ONLINE_SESSION_START_BEFORE_MESSAGE_PUBLIC = "Get ready for your {className} in {minutes} minutes";
        public static final String OFFLINE_SESSION_START_BEFORE_MESSAGE = "Hi {firstName}, Your {className} is starting in {minutes} minutes ";
        public static final String OFFLINE_SESSION_START_BEFORE_MESSAGE_PUBLIC = "Your fitness class in {className} starts in {minutes} mins.";
        public static final int SESSION_START_BEFORE_MINUTES = 5;

        public static final String MEMBERSHIP_EXPIRE_TITLE = "Membership reminder.";
        public static final String MEMBERSHIP_EXPIRE_MESSAGE = "'{the membership}' will expire from {time} from now'";
        public static final String CASH_PURCHASE = "CASH";
        public static final String AUTHORIZATION = "Authorization";
        public static final String SUPPORT_MAIL_TITLE = "Fitzky support Q&A";
        public static final String DAILY_PASS = "Single entry";
        public static final String EMAIL_UPDATED_SUBJECT = "Your email address has been changed";
        public static final String PASSWORD_RESET_SUBJECT = "Your password has been reset";

        public static final String SUBSCRIPTION_PACKAGE_PURCHASED = "Subscription package purchase is successful";
        public static final String SUBSCRIPTION_PACKAGE_PURCHASED_DESC = "Hi {firstName}, Your purchase of '{the package}' is confirmed.";

        public static final int MAX_USER_LOGIN_ATTEMPTS = 20;
        public static final int USER_LOCK_PERIOD = 10;
    }

    public static final class StripeConstants {
        public static final String PAYMENT_SUCCESS = "succeeded";
        public static final String PAYMENT_INTENT_SUCCESS = "payment_intent.succeeded";
        public static final String PAYMENT_INTENT_FAIL = "payment_intent.payment_failed";
        public static final String SETUP_SUCCESS = "succeeded";
        public static final String SETUP_INTENT_SUCCESS = "setup_intent.succeeded";

        //recurring
        public static final String SUBSCRIPTION_CREATED = "customer.subscription.created";
        public static final String INVOICE_CREATED = "invoice.created";
        public static final String INVOICE_FINALIZED = "invoice.finalized";
        public static final String INVOICE_PAYMENT_SUCCEEDED = "invoice.payment_succeeded";
        public static final String INVOICE_PAYMENT_FAILED = "invoice.payment_failed";

    }

    public static final class ZoomConstants {
        public final static int ROLE_JOINEE = 0;
        public final static int ROLE_HOST = 1;
    }

    public static final class Avatar {
        public final static String AVATAR = "https://d3iitm8eqnsqba.cloudfront.net/business/avatar.png";
    }

    public static final class AmountConstants {
        public static final BigDecimal INVITE_A_FRIEND_DISCOUNT = new BigDecimal(300);
        public static final BigDecimal STRIPE_TRANSACTION_COST_PERCENTAGE = new BigDecimal(2.9);
        public static final BigDecimal PAYHERE_TRANSACTION_COST_PERCENTAGE = new BigDecimal(2.69);
        public static final BigDecimal STRIPE_CURRENCY_COST_PERCENTAGE = new BigDecimal(1);
        public static final BigDecimal STRIPE_TRANSACTION_COST_AMOUNT = new BigDecimal(0.30);
        public static final BigDecimal FITZKY_STRIPE_CONTRIBUTION_PERCENTAGE = new BigDecimal(100);
        public static final BigDecimal FITZKY_PAYHERE_CONTRIBUTION_PERCENTAGE = new BigDecimal(100);

        public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

        public static final Integer TRIAL_PERIOD = 7;
    }

    public static final class CorporateConstants {
        public static final long AIA_CORPORATE_ID = 4;
        public static long ACCA_CORPORATE_MEMBERSHIP_ID = 0;
    }

    public static final class ApplicationVersion {
        public static final String VERSION = "1.8.81";
        //Updated already consumed promocode checking part
    }
}
