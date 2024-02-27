package com.fitnexus.server.service;

import com.fitnexus.server.dto.language.LanguageDTO;
import com.fitnexus.server.entity.auth.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LanguageService {

    List<String> getAllLanguages();

    Page<LanguageDTO> getAllLanguages(Pageable pageable);

    void saveLanguage(String languageName);

    void updateLanguage(String languageName, long languageId);

    void deleteLanguage(long languageId);

    Language getLanguage(String name);
}
