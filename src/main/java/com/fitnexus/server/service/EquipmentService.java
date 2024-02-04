package com.fitnexus.server.service;

import com.fitnexus.server.dto.gym.EquipmentDTO;
import com.fitnexus.server.entity.gym.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EquipmentService {
    void createEquipment(String name);

    void createEquipmentList(List<String> nameList);

    Equipment getById(long id);

    EquipmentDTO getDTO(Equipment equipment);

    List<Equipment> getEquipmentList(List<Long> idList);

    List<EquipmentDTO> getEquipmentDTOList(List<Equipment> equipmentList);

    List<EquipmentDTO> getAllEquipment();

    Page<EquipmentDTO> getEquipmentPage(Pageable pageable);
}
