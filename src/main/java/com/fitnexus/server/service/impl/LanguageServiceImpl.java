package com.fitnexus.server.service.impl;

import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.language.LanguageDTO;
import com.fitnexus.server.entity.auth.Language;
import com.fitnexus.server.entity.auth.UserLanguage;
import com.fitnexus.server.entity.classes.ClassSession;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.repository.auth.LanguageRepository;
import com.fitnexus.server.service.LanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    public List<String> getAllLanguages() {
        return languageRepository.findAll().stream().map(Language::getLanguageName).collect(Collectors.toList());
    }

    @Override
    public Page<LanguageDTO> getAllLanguages(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        return languageRepository.findAll(pageRequest).map(language -> new LanguageDTO(language.getId(), language.getLanguageName()));
    }

    @Override
    public void saveLanguage(String languageName) {
        if (languageRepository.existsByLanguageName(languageName))
            throw new CustomServiceException("Language already existing");
        Language language = new Language();
        language.setLanguageName(languageName);
        languageRepository.save(language);
    }

    @Override
    public void updateLanguage(String languageName, long languageId) {
        Language language = languageRepository.findById(languageId).orElseThrow(() -> new CustomServiceException("Language not found"));
        language.setLanguageName(languageName);
        languageRepository.save(language);
    }

    @Override
    public void deleteLanguage(long languageId) {
        Language language = languageRepository.findById(languageId).orElseThrow(() -> new CustomServiceException("Language not found"));

        List<ClassSession> classSessions = language.getClassSessions();
        List<PhysicalClassSession> physicalClassSessions = language.getPhysicalClassSessions();
        List<UserLanguage> userLanguages = language.getUserLanguages();

        if ((classSessions != null && classSessions.size() > 0) ||
                (userLanguages != null && userLanguages.size() > 0) ||
                (physicalClassSessions != null && physicalClassSessions.size() > 0))
            throw new CustomServiceException("Can't delete " + language.getLanguageName() + ". It is already used.");

        languageRepository.delete(language);
    }

    @Override
    public Language getLanguage(String name) {
        Language language = languageRepository.findLanguageByLanguageName(name);
        if (language == null) {
            language = languageRepository.save(new Language(name));
        }
        return language;
    }
}
