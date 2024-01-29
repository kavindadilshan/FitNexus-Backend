package com.fitnexus.server.service;

import com.fitnexus.server.config.security.custom.CustomOauthException;
import com.fitnexus.server.dto.AuthUserDetailsDTO;
import com.fitnexus.server.dto.UserAuthDTO;
import com.fitnexus.server.entity.AuthUser;
import com.fitnexus.server.enums.UserRole;
import com.fitnexus.server.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by :- Intellij Idea
 * Author :- Tharindu
 * Date :- 2020-04-17
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService{

    private final AuthUserRepository authUserRepository;

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomOauthException("Invalid username"));
        return new UserAuthDTO(authUser.getId(), username, authUser.getPassword(), getRole(authUser.getUserRole()),
                authUser.getStatus(), modelMapper.map(authUser, AuthUserDetailsDTO.class));
    }

    /**
     * @param userRole the user role of a searched user
     * @return the user role as authority
     */
    private List<SimpleGrantedAuthority> getRole(UserRole userRole) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole));
    }

}
