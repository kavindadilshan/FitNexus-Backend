package com.fitnexus.server.config.email;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.publicuser.PublicUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;


@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private String pageName;
    private Locale locale;
    private AuthUser authUser;
    private PublicUser publicUser;
    private String password;
    private boolean isPasswordChange;
    private boolean isExistingActivate;

    public OnRegistrationCompleteEvent(
            AuthUser authUser, String password, Locale locale, String pageName) {
        super(authUser);

        this.authUser = authUser;
        this.locale = locale;
        this.pageName = pageName;
        this.password = password;
        this.isPasswordChange=false;
        this.isExistingActivate = false;
    }

    public OnRegistrationCompleteEvent(
            AuthUser authUser, Locale locale, String pageName) {
        super(authUser);

        this.authUser = authUser;
        this.locale = locale;
        this.pageName = pageName;
        this.isPasswordChange = false;
        this.isExistingActivate = true;
    }

    public OnRegistrationCompleteEvent(
            AuthUser authUser, Locale locale) {
        super(authUser);

        this.authUser = authUser;
        this.locale = locale;
        this.isPasswordChange=true;
    }

    public OnRegistrationCompleteEvent(
            PublicUser publicUser, Locale locale, String pageName) {
        super(publicUser);

        this.publicUser = publicUser;
        this.locale = locale;
        this.pageName = pageName;
    }
}
