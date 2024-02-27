package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.language.LanguageDTO;
import com.fitnexus.server.service.LanguageService;
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
@RequestMapping(value = "/admin")
public class AdminLanguageController {

    private final LanguageService languageService;

    @PostMapping(value = "/language/{name}")
    public ResponseEntity saveLanguage(@PathVariable("name") String name) {
        log.info("Save language : \nname: {}", name);
        languageService.saveLanguage(name);
        log.info("Response : Language saved successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Language saved successfully"));
    }

    @GetMapping(value = "/language")
    public ResponseEntity getAllLanguages() {
        log.info("Get all languages");
        List<String> allLanguages = languageService.getAllLanguages();
        log.info("Response : Languages list");
        return ResponseEntity.ok(new CommonResponse<>(true, allLanguages));
    }

    @GetMapping(value = "/language/page")
    public ResponseEntity getAllLanguages(Pageable pageable) {
        log.info("Get all languages with pagination");
        Page<LanguageDTO> allLanguages = languageService.getAllLanguages(pageable);
        log.info("Response : Languages page");
        return ResponseEntity.ok(new CommonResponse<>(true, allLanguages));
    }

    @PatchMapping(value = "/language/{id}")
    public ResponseEntity updateLanguage(@PathVariable("id") long id, @RequestParam String name) {
        log.info("Update language : \nlanguage id: {}  \nname: {}", id, name);
        languageService.updateLanguage(name, id);
        log.info("Response : Language updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Language updated successfully"));
    }

    @DeleteMapping(value = "/language/{id}")
    public ResponseEntity deleteLanguage(@PathVariable("id") long id) {
        log.info("Delete language : \nlanguage id: {} ", id);
        languageService.deleteLanguage(id);
        log.info("Response : Language deleted successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Language deleted successfully"));
    }
}
