package com.fitnexus.server.service.impl;

import com.fitnexus.server.dto.admin.AdminNotificationDTO;
import com.fitnexus.server.dto.admin.CustomNotificationDTO;
import com.fitnexus.server.dto.admin.DateRangeDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.entity.admin.Admin;
import com.fitnexus.server.entity.admin.AdminNotification;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.admin.AdminNotificationSendingType;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.ClassSession;
import com.fitnexus.server.entity.classes.ClassSessionEnroll;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.classes.physical.PhysicalSessionEnroll;
import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.gym.GymMembership;
import com.fitnexus.server.entity.notification.Notification;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.entity.publicuser.PublicUserPushToken;
import com.fitnexus.server.enums.*;
import com.fitnexus.server.notification.PushNotificationManager;
import com.fitnexus.server.repository.admin.AdminNotificationRepository;
import com.fitnexus.server.repository.admin.AdminNotificationSendingTypeRepository;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.repository.classes.ClassRepository;
import com.fitnexus.server.repository.classes.ClassSessionEnrollRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalSessionEnrollRepository;
import com.fitnexus.server.repository.gym.GymMembershipRepository;
import com.fitnexus.server.repository.gym.GymRepository;
import com.fitnexus.server.repository.notification.NotificationRepository;
import com.fitnexus.server.repository.publicuser.PublicUserMembershipRepository;
import com.fitnexus.server.repository.publicuser.PublicUserNotificationRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.service.AdminNotificationService;
import com.fitnexus.server.util.EmailSender;
import com.fitnexus.server.util.sms.SmsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.NO_ADMIN_FOUND;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.NO_USER_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;
    private final AuthUserRepository authUserRepository;
    private final PublicUserRepository publicUserRepository;
    private final ClassSessionEnrollRepository classSessionEnrollRepository;
    private final PhysicalSessionEnrollRepository physicalSessionEnrollRepository;
    private final PublicUserMembershipRepository publicUserMembershipRepository;
    private final GymMembershipRepository gymMembershipRepository;
    private final AdminNotificationSendingTypeRepository adminNotificationSendingTypeRepository;
    private final EmailSender emailSender;
    private final SmsHandler smsHandler;
    private final PushNotificationManager pushNotificationManager;
    private final ClassRepository classRepository;
    private final PhysicalClassRepository physicalClassRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final GymRepository gymRepository;
    private final PublicUserNotificationRepository publicUserNotificationRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void sendNotification(AdminNotificationDTO dto, String username) {

        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        Admin admin = authUser.getAdmin();
        if (admin == null) throw new CustomServiceException(NO_ADMIN_FOUND);

        NotificationUserGroup userGroup = dto.getUserGroup();

        List<AdminNotificationSendingType> sendingTypeEntityList = new ArrayList<>();
        AdminNotification notification = new AdminNotification();

        notification.setAdmin(admin);
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setUserGroup(userGroup);

        List<PublicUser> publicUsers = null;

        if (userGroup.equals(NotificationUserGroup.ALL)) {
            publicUsers = publicUserRepository.findAll();
        } else if (userGroup.equals(NotificationUserGroup.ONLINE_CLASS_ALL)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(null, null);
        } else if (userGroup.equals(NotificationUserGroup.ONLINE_CLASS_SELECTED)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(dto.getClassIdList(), null);
        } else if (userGroup.equals(NotificationUserGroup.GROUP_CLASS_ALL)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(null, ClassCategory.GROUP);
        } else if (userGroup.equals(NotificationUserGroup.GROUP_CLASS_SELECTED)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(dto.getClassIdList(), ClassCategory.GROUP);
        } else if (userGroup.equals(NotificationUserGroup.PERSONAL_CLASS_ALL)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(null, ClassCategory.PERSONAL);
        } else if (userGroup.equals(NotificationUserGroup.PERSONAL_CLASS_SELECTED)) {
            publicUsers = classSessionEnrollRepository.findPublicUsersByClassSession_ClassParentAndClassSession_DateAndDateTimeAfter(dto.getClassIdList(), ClassCategory.PERSONAL);
        } else if (userGroup.equals(NotificationUserGroup.PHYSICAL_CLASS_ALL)) {
            publicUsers = physicalSessionEnrollRepository.findPublicUsersByPhysicalClassSession_PhysicalClassAndPhysicalClassSession_DateAndDateTimeAfter(null);
        } else if (userGroup.equals(NotificationUserGroup.PHYSICAL_CLASS_SELECTED)) {
            publicUsers = physicalSessionEnrollRepository.findPublicUsersByPhysicalClassSession_PhysicalClassAndPhysicalClassSession_DateAndDateTimeAfter(dto.getClassIdList());
        } else if (userGroup.equals(NotificationUserGroup.GYM_ALL)) {
            publicUsers = publicUserMembershipRepository.findPublicUsersByGym(null);
        } else if (userGroup.equals(NotificationUserGroup.GYM_SELECTED)) {
            publicUsers = publicUserMembershipRepository.findPublicUsersByGym(dto.getGymIdList());
        }

        if (publicUsers != null && publicUsers.size() > 0) {
            List<com.fitnexus.server.enums.AdminNotificationSendingType> sendingTypes = dto.getSendingTypes();
            if (sendingTypes.contains(com.fitnexus.server.enums.AdminNotificationSendingType.EMAIL)) {
                List<String> emails = publicUsers.stream().map(PublicUser::getEmail).collect(Collectors.toList());
                emails.removeAll(Collections.singleton(null));
                sendEmails(emails, dto.getMessage(), dto.getTitle());
                AdminNotificationSendingType sendingTypeEntity = new AdminNotificationSendingType();
                sendingTypeEntity.setNotification(notification);
                sendingTypeEntity.setSendingType(com.fitnexus.server.enums.AdminNotificationSendingType.EMAIL);
                sendingTypeEntityList.add(sendingTypeEntity);
            }
            if (sendingTypes.contains(com.fitnexus.server.enums.AdminNotificationSendingType.SMS)) {
                List<String> mobiles = publicUsers.stream().map(PublicUser::getMobile).collect(Collectors.toList());
                mobiles.removeAll(Collections.singleton(null));
                sendSMS(mobiles, dto.getMessage());
                AdminNotificationSendingType sendingTypeEntity = new AdminNotificationSendingType();
                sendingTypeEntity.setNotification(notification);
                sendingTypeEntity.setSendingType(com.fitnexus.server.enums.AdminNotificationSendingType.SMS);
                sendingTypeEntityList.add(sendingTypeEntity);
            }
            if (sendingTypes.contains(com.fitnexus.server.enums.AdminNotificationSendingType.PUSH)) {
                sendPushNotifications(publicUsers, dto.getMessage(), dto.getTitle());
                AdminNotificationSendingType sendingTypeEntity = new AdminNotificationSendingType();
                sendingTypeEntity.setNotification(notification);
                sendingTypeEntity.setSendingType(com.fitnexus.server.enums.AdminNotificationSendingType.PUSH);
                sendingTypeEntityList.add(sendingTypeEntity);
            }
            adminNotificationRepository.save(notification);
            adminNotificationSendingTypeRepository.saveAll(sendingTypeEntityList);

        } else throw new CustomServiceException("No Users found for this user group");
    }

    @Override
    public void sendCustomNotification(CustomNotificationDTO dto, String username) {
        Set<PublicUser> publicUsers = new LinkedHashSet<>();

        DateRangeDTO dateRangeDTO = dto.getDateRange();
        List<Long> recipientsIdList = dto.getRecipientsIdList();

        switch (dto.getRecipientType()) {
            case ONLINE_ONE_TO_ONE:
            case ONLINE_GROUP_CLASS:
                for (Long onlineClassId : recipientsIdList) {
                    Optional<Class> byId = classRepository.findById(onlineClassId);
                    if (byId.isPresent()) {
                        Class clz = byId.get();
                        List<ClassSession> classSessions = clz.getClassSessions();
                        if (classSessions != null) {
                            for (ClassSession classSession : classSessions) {
                                List<ClassSessionEnroll> classSessionEnrolls = classSession.getClassSessionEnrolls();
                                if (classSessionEnrolls != null) {
                                    for (ClassSessionEnroll classSessionEnroll : classSessionEnrolls) {
                                        publicUsers.add(classSessionEnroll.getPublicUser());
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case FITNESS_CLASS:
                for (Long physicalClassId : recipientsIdList) {
                    Optional<PhysicalClass> byId = physicalClassRepository.findById(physicalClassId);
                    if (byId.isPresent()) {
                        PhysicalClass physicalClass = byId.get();
                        List<PhysicalClassSession> physicalClassSessions = physicalClass.getPhysicalClassSessions();
                        if (physicalClassSessions != null) {
                            for (PhysicalClassSession physicalClassSession : physicalClassSessions) {
                                List<PhysicalSessionEnroll> physicalSessionEnrolls = physicalClassSession.getPhysicalSessionEnrolls();
                                if (physicalSessionEnrolls != null) {
                                    for (PhysicalSessionEnroll physicalSessionEnroll : physicalSessionEnrolls) {
                                        publicUsers.add(physicalSessionEnroll.getPublicUser());
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case BUSINESS:
                for (Long businessProfileId : recipientsIdList) {
                    Optional<BusinessProfile> byId = businessProfileRepository.findById(businessProfileId);
                    if (byId.isPresent()) {
                        BusinessProfile businessProfile = byId.get();

                        //online class students
                        List<Class> classes = businessProfile.getClasses();
                        if (classes != null) {
                            for (Class clz : classes) {
                                List<ClassSession> classSessions = clz.getClassSessions();
                                if (classSessions != null) {
                                    for (ClassSession classSession : classSessions) {
                                        List<ClassSessionEnroll> classSessionEnrolls = classSession.getClassSessionEnrolls();
                                        if (classSessionEnrolls != null) {
                                            for (ClassSessionEnroll classSessionEnroll : classSessionEnrolls) {
                                                publicUsers.add(classSessionEnroll.getPublicUser());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //physical class students
                        List<PhysicalClass> physicalClasses = businessProfile.getPhysicalClasses();
                        if (physicalClasses != null) {
                            for (PhysicalClass physicalClass : physicalClasses) {
                                List<PhysicalClassSession> physicalClassSessions = physicalClass.getPhysicalClassSessions();
                                if (physicalClassSessions != null) {
                                    for (PhysicalClassSession physicalClassSession : physicalClassSessions) {
                                        List<PhysicalSessionEnroll> physicalSessionEnrolls = physicalClassSession.getPhysicalSessionEnrolls();
                                        if (physicalSessionEnrolls != null) {
                                            for (PhysicalSessionEnroll physicalSessionEnroll : physicalSessionEnrolls) {
                                                publicUsers.add(physicalSessionEnroll.getPublicUser());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //students in gyms
                        List<BusinessProfileLocation> businessProfileLocations = businessProfile.getBusinessProfileLocations();
                        if (businessProfileLocations != null) {
                            for (BusinessProfileLocation businessProfileLocation : businessProfileLocations) {
                                Gym gym = businessProfileLocation.getGym();
                                if (gym != null) {
                                    List<GymMembership> gymMemberships = gym.getGymMemberships();
                                    if (gymMemberships != null) {
                                        for (GymMembership gymMembership : gymMemberships) {
                                            List<PublicUserMembership> publicUserMemberships = gymMembership.getMembership().getPublicUserMemberships();
                                            if (publicUserMemberships != null) {
                                                for (PublicUserMembership publicUserMembership : publicUserMemberships) {
                                                    publicUsers.add(publicUserMembership.getPublicUser());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case GYM:
                for (Long gymId : recipientsIdList) {
                    Optional<Gym> byId = gymRepository.findById(gymId);
                    if (byId.isPresent()) {
                        Gym gym = byId.get();
                        List<GymMembership> gymMemberships = gym.getGymMemberships();
                        if(gymMemberships != null) {
                            for (GymMembership gymMembership : gymMemberships) {
                                List<PublicUserMembership> publicUserMemberships = gymMembership.getMembership().getPublicUserMemberships();
                                if(publicUserMemberships != null) {
                                    for (PublicUserMembership publicUserMembership : publicUserMemberships) {
                                        publicUsers.add(publicUserMembership.getPublicUser());
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case ALL:
                List<PublicUser> all = publicUserRepository.findAll();
                publicUsers.addAll(all);
                break;
            case INDIVIDUAL:
                PublicUser byMobile = publicUserRepository.findByMobile(dto.getMobile());
                publicUsers.add(byMobile);
                break;
            case LAST_ACTIVE_DATE:
                List<PublicUser> allByActiveDateTimeBetween = publicUserRepository.findAllByLastSeenDateTimeBetween(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
                publicUsers.addAll(allByActiveDateTimeBetween);
                break;
            case ACCOUNT_CREATION_DATE:
                List<PublicUser> allByCreatedDateTimeBetween = publicUserRepository.findAllByCreatedDateTimeBetween(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
                publicUsers.addAll(allByCreatedDateTimeBetween);
                break;
            case PERSONAL_COACHING:
                for (Long authUserId : recipientsIdList) {
                    List<PublicUser> allPublicUsersByAuthUserId = publicUserRepository.getAllPublicUsersByAuthUserId(authUserId);
                    publicUsers.addAll(allPublicUsersByAuthUserId);
                }
                break;
            case NOTHING_BOOKED:
                List<PublicUser> allPublicUsersNotPurchasedAnything = publicUserRepository.getAllPublicUsersNotPurchasedAnything(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
                publicUsers.addAll(allPublicUsersNotPurchasedAnything);
                break;
        }

        performCustomNotificationSending(dto.getMessage(), new ArrayList<>(publicUsers), dto.isSendPush(), dto.isSendSms(), username);
    }

    @Override
    public void performCustomNotificationSending(String message, List<PublicUser> publicUsers, boolean sendPush, boolean sendSms, String username) {

        log.info("Sending Notifications To: - {} public users", publicUsers.size());

        if (sendPush) {
            List<Notification> notifications = new ArrayList<>();

            for (PublicUser publicUser : publicUsers) {

                Notification notification = new Notification();

                notification.setToWhom(MessageTo.PUBLIC_USER);
                notification.setUserId(publicUser.getId());
                notification.setFromWhom(username);
                notification.setDateTime(LocalDateTime.now());
                notification.setMessage(message);
                notification.setType(MessageType.PUSH);

                notifications.add(notification);
            }

            sendPushNotifications(publicUsers, "Fitzky", message);
            notificationRepository.saveAll(notifications);
        }

        if (sendSms) {

            List<Notification> notifications = new ArrayList<>();
            List<String> numbers = new ArrayList<>();

            for (PublicUser publicUser : publicUsers) {

                numbers.add(publicUser.getMobile());

                Notification notification = new Notification();

                notification.setToWhom(MessageTo.PUBLIC_USER);
                notification.setUserId(publicUser.getId());
                notification.setFromWhom(username);
                notification.setDateTime(LocalDateTime.now());
                notification.setMessage(message);
                notification.setType(MessageType.SMS);

                notifications.add(notification);
            }
            smsHandler.sendBulkMessages(numbers, message);
            notificationRepository.saveAll(notifications);
        }
    }

    private void sendEmails(List<String> emailList, String message, String title) {
        emailSender.sendHtmlEmail(emailList, title, emailSender.getNotificationHtmlEmail(message), null, null);
    }

    private void sendSMS(List<String> mobileList, String message) {
        smsHandler.sendBulkMessages(mobileList, message);
    }

    private void sendPushNotifications(List<PublicUser> publicUsers, String title, String message) {
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        for (PublicUser publicUser : publicUsers) {
            for (PublicUserPushToken publicUserPushToken : publicUser.getPublicUserPushTokens()) {
                if (publicUserPushToken.getDeviceType() == DeviceType.WEB)
                    userTokensWeb.add(publicUserPushToken.getToken());
                else userTokensMobile.add(publicUserPushToken.getToken());
            }
        }

        if (userTokensMobile.size() > 0) {
            pushNotificationManager.sendPushNotification(null, title, message, userTokensMobile, UserDeviceTypes.PUBLIC_USER_MOBILE);
        }
        if (userTokensWeb.size() > 0) {
            pushNotificationManager.sendPushNotification(null, title, message, userTokensWeb, UserDeviceTypes.PUBLIC_USER_WEB);
        }
    }
}
