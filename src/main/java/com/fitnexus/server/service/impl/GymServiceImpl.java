package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classes.ClassNameIdDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymRateDTO;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import com.fitnexus.server.entity.businessprofile.Facility;
import com.fitnexus.server.entity.businessprofile.LocationFacility;
import com.fitnexus.server.entity.gym.*;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileLocationRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.repository.businessprofile.LocationFacilityRepository;
import com.fitnexus.server.repository.gym.GymEquipmentRepository;
import com.fitnexus.server.repository.gym.GymImageRepository;
import com.fitnexus.server.repository.gym.GymRatingRepository;
import com.fitnexus.server.repository.gym.GymRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.FileHandler;
import com.fitnexus.server.util.UsernameGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.RATE_DECIMAL_PLACES;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static com.fitnexus.server.util.FileHandler.GYM_FOLDER;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;
    private final GymImageRepository gymImageRepository;
    private final GymRatingRepository gymRatingRepository;
    private final BusinessProfileLocationRepository businessProfileLocationRepository;
    private final AuthUserRepository authUserRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PublicUserRepository publicUserRepository;
    private final GymEquipmentRepository gymEquipmentRepository;
    private final LocationFacilityRepository locationFacilityRepository;

    private final EquipmentService equipmentService;
    private final FacilityService facilityService;
    private final LocationService locationService;
    private final FileHandler fileHandler;
    private final MembershipService membershipService;
    private final ModelMapper modelMapper;
    private final PromoCodeManagementService promoCodeManagementService;

    private final UsernameGeneratorUtil usernameGeneratorUtil;

    private static final double LOCATION_LIMIT = 50.00;

    @Override
    @Transactional
    public void createGym(GymDTO dto, String username) {

        BusinessProfileLocation location = businessProfileLocationRepository.findById(dto.getBusinessProfileLocationId()).orElseThrow(() -> new CustomServiceException(NO_LOCATION_FOUND));
        Optional<Gym> byLocation = gymRepository.findByLocation(location);
        if (byLocation.isPresent()) throw new CustomServiceException("This location already has a GYM");

        Gym gym = new Gym();
        gym.setLocation(location);
        gym.setName(dto.getGymName());
        gym.setDescription(dto.getDescription());
        gym.setOpenInWeekDays(dto.isOpenInWeekDays());
        gym.setOpenInWeekEnd(dto.isOpenInWeekEnd());
        gym.setWeekDaysOpeningHour(dto.getWeekDaysOpeningHour());
        gym.setWeekDaysClosingHour(dto.getWeekDaysClosingHour());
        gym.setSaturdayOpeningHour(dto.getSaturdayOpeningHour());
        gym.setSaturdayClosingHour(dto.getSaturdayClosingHour());
        gym.setSundayOpeningHour(dto.getSundayOpeningHour());
        gym.setSundayClosingHour(dto.getSundayClosingHour());
        gym.setClosedOnSpecificDay(dto.isClosedOnSpecificDay());
        gym.setClosedSpecificDay(dto.getClosedSpecificDay());
        gym.setProfileImage(saveImage(dto.getProfileImage(), "GYM-profile-" + dto.getBusinessProfileLocationId() + UUID.randomUUID()));

        gym.setYoutubeUrl(dto.getYoutubeUrl());

        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        gym.setCreatedBy(authUser);
        gymRepository.save(gym);
        log.info("Save gym - " + gym);

        List<String> base64Images = dto.getGymImages();
        if (base64Images != null && base64Images.size() > 0) {
            List<GymImage> gymImages = new ArrayList<>();
            for (String base64Image : base64Images) {
                gymImages.add(new GymImage(
                        saveImage(base64Image, "GYM-image-" + base64Images.indexOf(base64Image) + "-" + dto.getBusinessProfileLocationId() + UUID.randomUUID()),
                        gym));
            }
            gymImageRepository.saveAll(gymImages);
            log.info("Save gym images - " + gymImages);
        }

        List<Long> equipmentIdList = dto.getEquipmentIdList();
        if (equipmentIdList != null && equipmentIdList.size() > 0) {
            List<Equipment> equipmentList = equipmentService.getEquipmentList(equipmentIdList);
            List<GymEquipment> gymEquipments = equipmentList.stream().map(equipment -> new GymEquipment(equipment, gym)).collect(Collectors.toList());
            gymEquipmentRepository.saveAll(gymEquipments);
            log.info("Save gym equipments - " + gymEquipments);
        }

        List<Long> facilityIdList = dto.getFacilityIdList();
        if (facilityIdList != null && facilityIdList.size() > 0) {
            List<Facility> facilityList = facilityService.getFacilityList(facilityIdList);
            List<LocationFacility> locationFacilities = facilityList.stream().map(facility -> new LocationFacility(facility, location)).collect(Collectors.toList());
            locationFacilityRepository.saveAll(locationFacilities);
        }

        usernameGeneratorUtil.setGymUniqueName(gym);
    }

    @Override
    @Transactional
    public void updateGym(GymDTO dto, String username) {

        Gym gym = gymRepository.findById(dto.getGymId()).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        gym.setDescription(dto.getDescription());
        gym.setOpenInWeekDays(dto.isOpenInWeekDays());
        gym.setOpenInWeekEnd(dto.isOpenInWeekEnd());
        gym.setWeekDaysOpeningHour(dto.getWeekDaysOpeningHour());
        gym.setWeekDaysClosingHour(dto.getWeekDaysClosingHour());
        gym.setSaturdayOpeningHour(dto.getSaturdayOpeningHour());
        gym.setSaturdayClosingHour(dto.getSaturdayClosingHour());
        gym.setSundayOpeningHour(dto.getSundayOpeningHour());
        gym.setSundayClosingHour(dto.getSundayClosingHour());
        gym.setClosedOnSpecificDay(dto.isClosedOnSpecificDay());
        gym.setClosedSpecificDay(dto.getClosedSpecificDay());

        gym.setYoutubeUrl(dto.getYoutubeUrl());

        if (!(dto.getProfileImage().startsWith("https://") || dto.getProfileImage().startsWith("http://"))) {
            gym.setProfileImage(saveImage(dto.getProfileImage(), "GYM-profile-" + dto.getBusinessProfileLocationId() + UUID.randomUUID()));
        }

        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        gym.setUpdatedBy(authUser);
        gymRepository.save(gym);
        log.info("Update gym - " + gym);

        List<String> imageStrings = dto.getGymImages();
        List<GymImage> gymImages = gym.getGymImages();
        if (gymImages != null && gymImages.size() > 0) {
            gymImageRepository.deleteAll(gymImages);
        }
        if (imageStrings != null && imageStrings.size() > 0) {
            gymImages = new ArrayList<>();
            for (String imageString : imageStrings) {
                if (imageString != null && !imageString.isEmpty()) {
                    if (imageString.startsWith("https://") || imageString.startsWith("http://")) {
                        gymImages.add(new GymImage(imageString, gym));
                    } else {
                        gymImages.add(new GymImage(
                                saveImage(imageString, "GYM-image-" + imageStrings.indexOf(imageString) + "-" + dto.getBusinessProfileLocationId() + UUID.randomUUID()),
                                gym));
                    }
                }
            }
            gymImageRepository.saveAll(gymImages);
        }

        List<GymEquipment> gymEquipments = gym.getGymEquipments();
        if (gymEquipments != null && gymEquipments.size() > 0) {
            gymEquipmentRepository.deleteAll(gymEquipments);
        }
        List<Long> equipmentIdList = dto.getEquipmentIdList();
        if (equipmentIdList != null && equipmentIdList.size() > 0) {
            List<Equipment> equipmentList = equipmentService.getEquipmentList(equipmentIdList);
            gymEquipments = equipmentList.stream().map(equipment -> new GymEquipment(equipment, gym)).collect(Collectors.toList());
            gymEquipmentRepository.saveAll(gymEquipments);
        }

        BusinessProfileLocation location = gym.getLocation();
        List<LocationFacility> locationFacilities = location.getLocationFacilities();
        if (locationFacilities != null && locationFacilities.size() > 0) {
            locationFacilityRepository.deleteAll(locationFacilities);
        }
        List<Long> facilityIdList = dto.getFacilityIdList();
        if (facilityIdList != null && facilityIdList.size() > 0) {
            List<Facility> facilityList = facilityService.getFacilityList(facilityIdList);
            locationFacilities = facilityList.stream().map(facility -> new LocationFacility(facility, location)).collect(Collectors.toList());
            locationFacilityRepository.saveAll(locationFacilities);
        }
    }

    private String saveImage(String image, String name) {
        return fileHandler.saveBase64File(image, name, GYM_FOLDER);
    }

    @Override
    public Page<GymDTO> getAllGyms(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        Page<Gym> gymPage = gymRepository.findAll(pageRequest);
        return gymPage.map(this::getGymDTO);
    }

    @Override
    public List<GymDTO> getAllGymsAll() {
        List<Gym> gyms = gymRepository.findAll();
        return modelMapper.map(gyms, new TypeToken<List<GymDTO>>(){}.getType());
    }

    @Override
    public GymDTO getByIdAndPublicUserToken(long id, double longitude, double latitude, String token) {
        Gym gym = gymRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        return getGymDTO(gym, longitude, latitude, token);
    }

    @Override
    public GymDTO getByUniqueNameAndPublicUserToken(String gymName, double longitude, double latitude, String token) {
        Gym gym = gymRepository.findByGymUniqueName(gymName).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        return getGymDTO(gym, longitude, latitude, token);
    }

    @Override
    public GymDTO getByIdOpen(long id) {
        Gym gym = gymRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        return getGymDTOOpen(gym);
    }

    @Override
    public GymDTO getById(long id) {
        Gym gym = gymRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        return getGymDTO(gym);
    }

    @Override
    public Page<GymDTO> getPopularGyms(Pageable pageable, String country, double longitude, double latitude) {
        return gymRepository.getPopularGyms(country, pageable).map(gym -> getGymDTO(gym, longitude, latitude));
    }

    @Override
    public Page<GymDTO> getPopularGymsOpen(Pageable pageable) {
        return gymRepository.getPopularGyms(null, pageable).map(this::getGymDTO);
    }

    @Override
    public Page<GymDTO> searchActiveGyms(String name, double longitude, double latitude, Pageable pageable) {
        Page<Object[]> objects = gymRepository.searchActiveGyms(name, longitude, latitude, LOCATION_LIMIT, pageable);
        return objects.map(this::getGymDTOFromObject);
    }

    private GymDTO getGymDTOFromObject(Object[] object) {
        BigInteger id = (BigInteger) object[0];
        double distance = (double) object[1];
        GymDTO gymDTO = getGymDTO(gymRepository.findById(id.longValue()).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND)));
        gymDTO.setDistance(distance);
        return gymDTO;
    }

    @Override
    public Page<GymDTO> searchGyms(String text, Pageable pageable) {
        return gymRepository.searchGyms(text, pageable).map(this::getGymDTO);
    }

    @Override
    public Page<GymDTO> getGymsByBusinessProfile(long profileId, Pageable pageable) {
        BusinessProfile profile = businessProfileRepository.findById(profileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        return gymRepository.getGymByBusinessProfile(profile, null, pageable).map(this::getGymDTO);
    }

    @Override
    public Page<GymDTO> getGymsByBusinessProfileWithDistance(long profileId, double longitude, double latitude, String country, Pageable pageable) {
        BusinessProfile profile = businessProfileRepository.findById(profileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
//        Page<Object[]> objects = gymRepository.getNearestGymsByBusinessProfile(profile.getId(), longitude, latitude, LOCATION_LIMIT, pageable);
//        return objects.map(this::getGymDTOFromObject);
        return gymRepository.getGymByBusinessProfile(profile, country, pageable).map(gym -> getGymDTO(gym, longitude, latitude));
    }

    @Override
    public Page<GymDTO> searchGymsByBusinessProfile(long profileId, String text, Pageable pageable) {
        BusinessProfile profile = businessProfileRepository.findById(profileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        return gymRepository.searchGymsByBusinessProfile(profile, text, pageable).map(this::getGymDTO);
    }

    private GymDTO getGymDTO(Gym gym, double longitude, double latitude, String token) {

        GymDTO gymDTO = getGymDTO(gym, longitude, latitude);

        if (token != null) {
            long id = CustomUserAuthenticator.getPublicUserIdFromToken(token);
            PublicUser publicUser = publicUserRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            membershipService.setMembershipDetailsForGymDTO(gym, gymDTO, publicUser);
        }

        return gymDTO;
    }

    private GymDTO getGymDTOOpen(Gym gym) {
        GymDTO gymDTO = getGymDTO(gym);
        membershipService.setMembershipDetailsForGymDTO(gym, gymDTO, null);
        return gymDTO;
    }

    private GymDTO getGymDTO(Gym gym) {

        GymDTO dto = new GymDTO();

        BusinessProfileLocation location = gym.getLocation();
        BusinessProfile businessProfile = location.getBusinessProfile();
        dto.setBusinessProfileId(businessProfile.getId());
        dto.setBusinessProfileName(businessProfile.getBusinessName());
        dto.setPublicBusinessName(businessProfile.getPublicBusinessName());
        dto.setBusinessProfileImage(businessProfile.getProfileImage());
        dto.setBusinessProfileRating(businessProfile.getRating());
        dto.setBusinessProfileRatingCount(businessProfile.getRatingCount());
        dto.setBusinessProfileLocationId(location.getId());
        dto.setLocationName(location.getName());
        dto.setCountry(location.getCountry());
        dto.setTimeZone(location.getTimeZone());
        dto.setAddressLine1(location.getAddressLine1());
        dto.setAddressLine2(location.getAddressLine2());
        dto.setLongitude(location.getLongitude());
        dto.setLatitude(location.getLatitude());
        dto.setCity(location.getCity());
        dto.setProvince(location.getProvince());
        dto.setPostalCode(location.getPostalCode());
        dto.setGymId(gym.getId());
        dto.setGymName(gym.getName());
        dto.setGymUniqueName(gym.getGymUniqueName());
        dto.setDescription(gym.getDescription());
        dto.setOpenInWeekDays(gym.isOpenInWeekDays());
        dto.setOpenInWeekEnd(gym.isOpenInWeekEnd());
        dto.setWeekDaysOpeningHour(gym.getWeekDaysOpeningHour());
        dto.setWeekDaysClosingHour(gym.getWeekDaysClosingHour());
        dto.setSaturdayOpeningHour(gym.getSaturdayOpeningHour());
        dto.setSaturdayClosingHour(gym.getSaturdayClosingHour());
        dto.setSundayOpeningHour(gym.getSundayOpeningHour());
        dto.setSundayClosingHour(gym.getSundayClosingHour());
        dto.setClosedOnSpecificDay(gym.isClosedOnSpecificDay());
        dto.setClosedSpecificDay(gym.getClosedSpecificDay());
        dto.setRating(gym.getRating());
        dto.setRatingCount(gym.getRatingCount());
        dto.setProfileImage(gym.getProfileImage());
        dto.setGymImages(gym.getGymImages().stream().map(GymImage::getImage).collect(Collectors.toList()));

        dto.setYoutubeUrl(gym.getYoutubeUrl());

        List<Equipment> equipmentList = gym.getGymEquipments().stream().map(GymEquipment::getEquipment).collect(Collectors.toList());
        dto.setEquipmentList(equipmentService.getEquipmentDTOList(equipmentList));

        dto.setFacilities(facilityService.getFacilityDTOListFromLocationFacilityList(location.getLocationFacilities()));
        return dto;
    }

    private GymDTO getGymDTO(Gym gym, double longitude, double latitude) {
        GymDTO gymDTO = getGymDTO(gym);
        BusinessProfileLocation location = gym.getLocation();
        double distance = locationService.getDistance(longitude, location.getLongitude(), latitude, location.getLatitude());
        gymDTO.setDistance(distance);
        return gymDTO;
    }

    @Override
    @Transactional
    public void rateGym(GymRateDTO rateDTO, int count) {
        try {
            if (rateDTO.getRating() < 0 || rateDTO.getRating() > 5)
                throw new CustomServiceException("Invalid rating amount");
            PublicUser publicUser = publicUserRepository
                    .findById(rateDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

            Gym gym = gymRepository.findById(rateDTO.getGymId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
            List<GymRating> ratingByUserForThisGym = gymRatingRepository.findGymRatingsByPublicUserAndGym(publicUser, gym);
            if (ratingByUserForThisGym.size() > 0) {
                updateRating(rateDTO, ratingByUserForThisGym.get(0), gym);
            } else {
                newRating(rateDTO, publicUser, gym);
            }
        } catch (LockAcquisitionException | CannotAcquireLockException de) {
            // re-tries up-to 3 times if transaction deadlock found.
            if (count > 3) return;
            rateGym(rateDTO, count + 1);
        }
    }

    private void updateRating(GymRateDTO rateDTO, GymRating gymRating, Gym gym) {

        gymRating.setComment(rateDTO.getComment());
        gymRating.setRating(rateDTO.getRating());
        gymRatingRepository.save(gymRating);
        log.info("Update gym rating - " + gymRating);

        // set gym rating
        double newGymRating = ((gym.getRating() * gym.getRatingCount() - gym.getRating())
                + rateDTO.getRating()) / (gym.getRatingCount());
        gym.setRating(CustomGenerator.round(newGymRating, RATE_DECIMAL_PLACES));
        gymRepository.save(gym);
        log.info("New gym rating - " + newGymRating);

        // set business profile rating
        BusinessProfile businessProfile = gym.getLocation().getBusinessProfile();
        double newBusinessRating = ((businessProfile.getRating() * businessProfile.getRatingCount() - businessProfile.getRating())
                + gym.getRating()) / (businessProfile.getRatingCount());
        businessProfile.setRating(CustomGenerator.round(newBusinessRating, RATE_DECIMAL_PLACES));
        businessProfileRepository.save(businessProfile);
        log.info("New business profile rating - " + newGymRating);
    }

    private void newRating(GymRateDTO rateDTO, PublicUser publicUser, Gym gym) {

        GymRating gymRating = new GymRating();
        gymRating.setComment(rateDTO.getComment());
        gymRating.setRating(rateDTO.getRating());
        gymRating.setPublicUser(publicUser);
        gymRating.setGym(gym);
        gymRatingRepository.save(gymRating);
        log.info("Save gym rating - " + gymRating);

        // set gym rating
        double newGymRating = (gym.getRating() * gym.getRatingCount() + rateDTO.getRating())
                / (gym.getRatingCount() + 1);
        gym.setRating(CustomGenerator.round(newGymRating, RATE_DECIMAL_PLACES));
        gym.setRatingCount(gym.getRatingCount() + 1);
        gymRepository.save(gym);
        log.info("New gym rating - " + newGymRating);

        // set business profile rating
        BusinessProfile businessProfile = gym.getLocation().getBusinessProfile();
        double newBusinessRating = (businessProfile.getRating() * businessProfile.getRatingCount() + gym.getRating())
                / (businessProfile.getRatingCount() + 1);
        businessProfile.setRating(CustomGenerator.round(newBusinessRating, RATE_DECIMAL_PLACES));
        businessProfile.setRatingCount(businessProfile.getRatingCount() + 1);
        businessProfileRepository.save(businessProfile);
        log.info("New business profile rating - " + newGymRating);
    }

    @Override
    public GymRateDTO getRateForGymByUser(long publicUserId, long gymId) {
        PublicUser publicUser = publicUserRepository
                .findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        List<GymRating> ratingByUserForThisGym = gymRatingRepository.findGymRatingsByPublicUserAndGym(publicUser, gym);
        if (ratingByUserForThisGym.size() <= 0) return null;
        GymRating gymRating = ratingByUserForThisGym.get(0);
        return GymRateDTO.builder().gymId(gymId).comment(gymRating.getComment()).rating(gymRating.getRating())
                .userId(gymRating.getPublicUser().getId()).build();
    }

    @Override
    public Page<PublicUserReviewsResponse> getRateForGym(long gymId, Pageable pageable) {
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        Page<GymRating> ratingByUserForThisGym = gymRatingRepository.findGymRatingsByGym(gym, pageable);
        return ratingByUserForThisGym.map(rating -> new PublicUserReviewsResponse(rating.getId(), rating.getRating(), rating.getComment(), rating.getDateTime(),
                rating.getPublicUser().getImage(), rating.getPublicUser().getFirstName(), rating.getPublicUser().getLastName()));
    }

    @Override
    public List<ClassNameIdDTO> getAllGymsForBusinessProfile(long businessProfileId) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        List<Gym> gyms = gymRepository.getAllByLocation_BusinessProfileOrderByIdDesc(businessProfile);
        return gyms.stream().map(gym -> new ClassNameIdDTO(gym.getId(), gym.getName(), null)).collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        Gym gym = gymRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        gymRepository.delete(gym);
    }
}
