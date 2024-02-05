package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.admin.Base64RequestDTO;
import com.fitnexus.server.dto.admin.RecipientDTO;
import com.fitnexus.server.dto.admin.CustomNotificationDTO;
import com.fitnexus.server.dto.admin.WeeklyTimeTableDTO;
import com.fitnexus.server.dto.auth.AuthUserPublicDTO;
import com.fitnexus.server.dto.businessprofile.BusinessProfileResponseDto;
import com.fitnexus.server.dto.classes.OnlineClassDTO;
import com.fitnexus.server.dto.classes.PhysicalClassDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.common.PasswordChangeDTO;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.instructor.ClientTransformationDTO;
import com.fitnexus.server.dto.membership.MembershipDTO;
import com.fitnexus.server.dto.promoCode.PromoCodeDTO;
import com.fitnexus.server.dto.promoCode.PromoCodeRequestDTO;
import com.fitnexus.server.enums.RecipientFetchType;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin")
public class AdminController {

    private final CommonUserService commonUserService;
    private final GymService gymService;
    private final BusinessProfileService businessProfileService;
    private final PhysicalClassService physicalClassService;
    private final ClassService classService;
    private final AdminNotificationService adminNotificationService;
    private final InstructorService instructorService;
    private final ClientTransformationService clientTransformationService;
    private final WeeklyTimeTableService weeklyTimeTableService;
    private final PromoCodeManagementService promoCodeManagementService;
    private final PublicUrlService publicUrlService;
    private final MembershipService membershipService;

    @PostMapping(value = "/password")
    public ResponseEntity changePassword(@RequestBody PasswordChangeDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Change password : \npassword change DTO: {} " + dto);
        String username = getUsername(token);
        dto.setUserName(username);
        commonUserService.changePassword(dto);
        log.info("Response : Password changed successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Password changed successfully"));
    }

    @GetMapping(value = "/username/{username}")
    public ResponseEntity existsByUsername(@PathVariable("username") String username) {
        log.info("Exists by username : \nusername - {}", username);
        boolean exists = commonUserService.existsByUsername(username);
        log.info("Response : Exists - {}", exists);
        return ResponseEntity.ok(new CommonResponse<>(true, exists));
    }

    @GetMapping(value = "/email/{email}")
    public ResponseEntity existsByEmail(@PathVariable("email") String email) {
        log.info("Exists by email : \nemail - {}", email);
        Map<String, Object> result = commonUserService.existsByEmail(email);
        log.info("Response : Existing result - {}", result);
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/recipients/type/{type}")
    public ResponseEntity<CommonResponse<List<RecipientDTO>>> getRecipients(@PathVariable("type") RecipientFetchType type) {
        log.info("getRecipients: type = {}", type);

        List<RecipientDTO> recipientDTOS = new ArrayList<>();

        switch (type) {
            case ONLINE_ONE_TO_ONE:
                List<OnlineClassDTO> PersonalClassDTOS = classService.getAllPersonalClasses();
                for (OnlineClassDTO classDTO : PersonalClassDTOS) {
                    recipientDTOS.add(new RecipientDTO(
                            classDTO.getId(),
                            classDTO.getName()
                    ));
                }
                break;
            case GYM:
                List<GymDTO> gymDTOS = gymService.getAllGymsAll();
                for (GymDTO gymDTO : gymDTOS) {
                    recipientDTOS.add(new RecipientDTO(
                            gymDTO.getGymId(),
                            gymDTO.getGymName()
                    ));
                }
                break;
            case BUSINESS:
                List<BusinessProfileResponseDto> businessProfileResponseDtos = businessProfileService.getAllBusinessProfiles();
                for (BusinessProfileResponseDto businessProfileResponseDto : businessProfileResponseDtos) {
                    recipientDTOS.add(new RecipientDTO(
                            businessProfileResponseDto.getId(),
                            businessProfileResponseDto.getBusinessName()
                    ));
                }
                break;
            case FITNESS_CLASS:
                List<PhysicalClassDTO> PhysicalClassDTOS = physicalClassService.getAllPhysicalClassesAll();
                for (PhysicalClassDTO classDTO : PhysicalClassDTOS) {
                    recipientDTOS.add(new RecipientDTO(
                            classDTO.getId(),
                            classDTO.getName()
                    ));
                }
                break;
            case ONLINE_GROUP_CLASS:
                List<OnlineClassDTO> GroupClassDTOS = classService.getAllGroupClasses();
                for (OnlineClassDTO classDTO : GroupClassDTOS) {
                    recipientDTOS.add(new RecipientDTO(
                            classDTO.getId(),
                            classDTO.getName()
                    ));
                }
                break;
            case PERSONAL_COACHING:
                List<AuthUserPublicDTO> authUsers = instructorService.getAllInstructorsAll();
                for (AuthUserPublicDTO authUser : authUsers) {
                    recipientDTOS.add(new RecipientDTO(
                            authUser.getId(),
                            authUser.getFirstName() + " " + authUser.getLastName()
                    ));
                }
                break;
            case ONLINE_CLASS_MEMBERSHIP:
                List<MembershipDTO> allOnlineClassMemberships = membershipService.getAllOnlineClassMemberships();
                for (MembershipDTO onlineClassMembership : allOnlineClassMemberships) {
                    recipientDTOS.add(new RecipientDTO(
                            onlineClassMembership.getMembershipId(),
                            onlineClassMembership.getName()
                    ));
                }
                break;
            case PHYSICAL_CLASS_MEMBERSHIP:
                List<MembershipDTO> allPhysicalClassMemberships = membershipService.getAllPhysicalClassMemberships();
                for (MembershipDTO physicalClassMembership : allPhysicalClassMemberships) {
                    recipientDTOS.add(new RecipientDTO(
                            physicalClassMembership.getMembershipId(),
                            physicalClassMembership.getName()
                    ));
                }
        }

        return ResponseEntity.ok(new CommonResponse<>(true, recipientDTOS));
    }

    @PostMapping(value = "/sendNotification")
    public ResponseEntity sendMessage(@RequestBody CustomNotificationDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Send Notification : \nCustomNotificationDTO - {}", dto);
        String username = getUsername(token);
        adminNotificationService.sendCustomNotification(dto, username);
        return ResponseEntity.ok(new CommonResponse<>(true, "Notifications sent successfully!"));
    }

    @PostMapping(value = "/client-transformation")
    public ResponseEntity saveClientTransformation(@RequestBody ClientTransformationDTO dto, @RequestHeader(name = "Authorization") String token){
        log.info("saveClientTransformation : \nClientTransformationDTO - {}", dto);
        clientTransformationService.createClientTransformation(dto);
        return ResponseEntity.ok(new CommonResponse<>(true, "Client Transformation saved successfully!"));
    }

    @PutMapping(value = "/client-transformation")
    public ResponseEntity updateClientTransformation(@RequestBody ClientTransformationDTO dto, @RequestHeader(name = "Authorization") String token){
        log.info("updateClientTransformation : \nClientTransformationDTO - {}", dto);
        clientTransformationService.updateClientTransformation(dto);
        return ResponseEntity.ok(new CommonResponse<>(true, "Client Transformation updated successfully!"));
    }

    @DeleteMapping(value = "/client-transformation/{id}")
    public ResponseEntity deleteClientTransformation(@PathVariable("id") long id){
        log.info("deleteClientTransformation : \nClientTransformationId - {}", id);
        clientTransformationService.deleteClientTransformation(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Client Transformation deleted successfully!"));
    }

    @GetMapping(value = "/client-transformation/instructor/{id}")
    public ResponseEntity<CommonResponse<List<ClientTransformationDTO>>> getClientTransformation(@PathVariable("id") long id){
        log.info("deleteClientTransformation : \nClientTransformationId - {}", id);
        List<ClientTransformationDTO> clientTransformationsByInstructorId = clientTransformationService.getClientTransformationsByInstructorId(id);
        return ResponseEntity.ok(new CommonResponse<>(true, clientTransformationsByInstructorId));
    }

    @GetMapping(value = "/client-transformation/pageable/instructor/{id}")
    public ResponseEntity getClientTransformation(@PathVariable("id") long id, Pageable pageable){
        log.info("deleteClientTransformation : \nClientTransformationIdPaged - {}", id);
        Page<ClientTransformationDTO> clientTransformationsByInstructorIdPaged = clientTransformationService.getClientTransformationsByInstructorIdPaged(id, pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, clientTransformationsByInstructorIdPaged));
    }

    @PostMapping(value = "/weekly-time-table")
    public ResponseEntity uploadWeeklyTimeTable(@RequestBody Base64RequestDTO base64RequestDTO, @RequestHeader(name = "Authorization") String token){
        String file = base64RequestDTO.getBase64();
        log.info("upload weekly time table : ", file);
        weeklyTimeTableService.uploadFile(file);
        return ResponseEntity.ok(new CommonResponse<>(true, "Time Table upload successfully!"));
    }

    @GetMapping(value = "/weekly-time-table")
    public ResponseEntity getWeeklyTimeTable(){
        log.info("Get weekly-time-table");
        WeeklyTimeTableDTO weeklyTimeTableDTO = weeklyTimeTableService.getFileUrl();
        return ResponseEntity.ok(new CommonResponse<>(true, weeklyTimeTableDTO));
    }

    @PostMapping(value = "/promo-codes")
    public ResponseEntity createPromoCode(@RequestBody PromoCodeRequestDTO promoCodeRequestDTO,
                                          @RequestHeader(name = "Authorization") String token){
        log.info("Create promo-codes");
        String username = getUsername(token);
        promoCodeManagementService.createPromoCode(promoCodeRequestDTO, username);
        return ResponseEntity.ok(new CommonResponse<>(true, "Promo-Code created successfully!"));
    }

    @GetMapping(value = "/promo-codes")
    public ResponseEntity getPromoCodes(Pageable pageable){
        log.info("Get promo-codes");
        Page<PromoCodeDTO> allPromoCodes = promoCodeManagementService.getAllPromoCodes(pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, allPromoCodes));
    }

    @DeleteMapping(value = "/promo-code/{id}")
    public ResponseEntity deletePromoCode(@PathVariable("id") long id){
        log.info("Delete Promo-code : \npromoCodeId - {}", id);
        promoCodeManagementService.deletePromoCode(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Promo code deleted successfully!"));
    }

    @GetMapping(value = "check/promo-code/{code}")
    public ResponseEntity checkPromoCode(@PathVariable("code") String code) {
        log.info("Check promo code is exists");
        boolean checkPromoCode = promoCodeManagementService.checkPromoCode(code);
        log.info("Response : promo code exists : {}", checkPromoCode);
        return ResponseEntity.ok(new CommonResponse<>(true, checkPromoCode));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }

    /* public username adding api */

    @GetMapping(value = "auth-user")
    public ResponseEntity changePublicUsername() {
        log.info("Change auth user public username");
        publicUrlService.addPublicUserNameToAuthUsers();
        return ResponseEntity.ok(new CommonResponse<>(true, "auth-user public username created successfully!"));
    }

    @GetMapping(value = "business-profile")
    public ResponseEntity changeBusinessProfileUniqueName() {
        log.info("Change business profile unique name");
        publicUrlService.addPublicBusinessNameToBusinessProfile();
        return ResponseEntity.ok(new CommonResponse<>(true, "business-profile public business name created successfully!"));
    }

    @GetMapping(value = "online-class")
    public ResponseEntity changeOnlineClassUniqueName() {
        log.info("Change online class unique name");
        publicUrlService.addPublicClassUniqueName();
        return ResponseEntity.ok(new CommonResponse<>(true, "online-class public class unique name created successfully!"));
    }

    @GetMapping(value = "physical-class")
    public ResponseEntity changePhysicalClassUniqueName() {
        log.info("Change physical class unique name");
        publicUrlService.addPublicPhysicalClassUniqueName();
        return ResponseEntity.ok(new CommonResponse<>(true, "physical-class public class unique name created successfully!"));
    }

    @GetMapping(value = "gym")
    public ResponseEntity changeGymUniqueName() {
        log.info("Change gym unique name");
        publicUrlService.addPublicGymUniqueName();
        return ResponseEntity.ok(new CommonResponse<>(true, "gym public unique name created successfully!"));
    }

}
