package com.fitnexus.server.dto.businessprofile;

import com.fitnexus.server.enums.BusinessProfilePaymentModel;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class BusinessProfileResponseDto {

    private long id;
    private String businessName;
    private String accountStatus;
    private String businessRegistrationNumber;
    private String profileImage;
    private List<String> images;
    private String telephone;
    private String email;
    private String description;
    private double rating;
    private long ratingCount;
    private BusinessProfileLocationDTO headOffice;
    private List<BusinessProfileLocationDTO> branches;
    private String accountNumber;
    private String accountName;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String branchCode;
    private String swiftCode;
    private BusinessProfilePaymentModel paymentModel;
    private double amount;
    private String packageDescription;
    private List<BusinessProfileAgreementDTO> agreementDetails;
    private BusinessProfileManagerDTO manager;
}
