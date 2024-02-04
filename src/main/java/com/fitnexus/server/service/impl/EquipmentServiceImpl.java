package com.fitnexus.server.service.impl;

import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.gym.EquipmentDTO;
import com.fitnexus.server.entity.gym.Equipment;
import com.fitnexus.server.repository.gym.EquipmentRepository;
import com.fitnexus.server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Override
    public void createEquipment(String name) {
        if (equipmentRepository.existsByName(name)) throw new CustomServiceException("Equipment already existing");
        equipmentRepository.save(new Equipment(name));
    }

    @Override
    public void createEquipmentList(List<String> nameList) {
        List<Equipment> equipment = new ArrayList<>();
        for (String name : nameList) {
            if (equipmentRepository.existsByName(name)) throw new CustomServiceException("Equipment already existing");
            equipment.add(new Equipment(name));
        }
        equipmentRepository.saveAll(equipment);
    }

    @Override
    public Equipment getById(long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new CustomServiceException("No equipment found"));
    }

    @Override
    public EquipmentDTO getDTO(Equipment equipment) {
        return new EquipmentDTO(equipment.getId(),equipment.getName());
    }

    @Override
    public List<Equipment> getEquipmentList(List<Long> idList){
        List<Equipment> equipment=new ArrayList<>();
        for (long id:idList){
            equipment.add(getById(id));
        }
        return equipment;
    }

    @Override
    public List<EquipmentDTO> getEquipmentDTOList(List<Equipment> equipmentList){
        List<EquipmentDTO> equipmentDTOList=new ArrayList<>();
        for (Equipment equipment:equipmentList){
            equipmentDTOList.add(getDTO(equipment));
        }
        return equipmentDTOList;
    }

    @Override
    public List<EquipmentDTO> getAllEquipment(){
        return getEquipmentDTOList(equipmentRepository.findAll());
    }

    @Override
    public Page<EquipmentDTO> getEquipmentPage(Pageable pageable){
        PageRequest pageRequest=PageRequest.of(pageable.getPageNumber(),pageable.getPageSize(), Sort.by(Sort.Direction.ASC,"id"));
        Page<Equipment> equipmentPage = equipmentRepository.findAll(pageRequest);
        return equipmentPage.map(this::getDTO);
    }
}
