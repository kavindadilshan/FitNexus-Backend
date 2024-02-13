package com.fitnexus.server.service;

import com.fitnexus.server.dto.advertisement.AdvertisementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdvertisementService {
    void createAdvertisement(AdvertisementDTO dto, String username);

    void updateAdvertisement(AdvertisementDTO dto, String username);

    void updateAdvertisementVisibility(AdvertisementDTO dto, String username);

    void deleteAdvertisement(long id);

    Page<AdvertisementDTO> getAllAdvertisements(Pageable pageable);

    AdvertisementDTO getAdvertisementById(long id);
    List<String> getAdvertisementImages();
}
