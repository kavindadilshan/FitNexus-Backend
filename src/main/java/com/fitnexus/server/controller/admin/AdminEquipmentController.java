package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.gym.EquipmentDTO;
import com.fitnexus.server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/equipment")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping(value = "/create/{name}")
    public ResponseEntity createEquipment(@PathVariable("name") String name) {
        log.info("Create equipment : \nname: {}", name);
        equipmentService.createEquipment(name);
        log.info("Response : Equipment created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Equipment created successfully"));
    }

    @PostMapping(value = "/create/list")
    public ResponseEntity createMultipleEquipment(@RequestBody List<String> names) {
        log.info("Create multiple equipment : \nnames: {}", names);
        equipmentService.createEquipmentList(names);
        log.info("Response : Equipment list created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Equipment list created successfully"));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllEquipment() {
        log.info("Get all equipment");
        List<EquipmentDTO> allEquipment = equipmentService.getAllEquipment();
        log.info("Response : All equipment list");
        return ResponseEntity.ok(new CommonResponse<>(true, allEquipment));
    }

    @GetMapping(value = "/all/page")
    public ResponseEntity getAllEquipmentPage(Pageable pageable) {
        log.info("Get equipment page: \npage request- {}", pageable);
        Page<EquipmentDTO> allEquipment = equipmentService.getEquipmentPage(pageable);
        log.info("Response : All equipment page");
        return ResponseEntity.ok(new CommonResponse<>(true, allEquipment));
    }
}
