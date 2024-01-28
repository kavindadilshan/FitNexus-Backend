package com.fitnexus.server.entity;

import com.fitnexus.server.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String password;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<UserRole> userRoles;
}
