package com.fitnexus.server.config.email;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.service.auth.UserService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final UserService service;
    private final PublicUserService publicUserService;
    private final CustomGenerator customGenerator;
    private final EmailSender emailSender;

    @Value("${support_mail}")
    private String supportMail;

    @Override
    public void onApplicationEvent(@NonNull OnRegistrationCompleteEvent event) {
        if (event.getAuthUser() != null) this.sendEmail(event);
        else this.confirmPublicUserRegistration(event);
    }

    /**
     * This will save a unique token for the user and sends it as email link.
     *
     * @param event the details of email page and locale.
     */
    private void sendEmail(OnRegistrationCompleteEvent event) {
        AuthUser user = event.getAuthUser();
        String subject;
        String url;
        String body;
        String recipientAddress = user.getEmail();

        if (event.isPasswordChange()) {
            subject = "Password Reset";
            body = emailSender.getVerifyTokenMailBodyForPasswordChange(service.createVerificationToken(user));
        } else {
            subject = "Activate Account";
            url = event.getPageName();
            body = event.isExistingActivate() ? emailSender.getLogInMailBodyForAlreadyActivaAccount(url) :
                    emailSender.getVerifyTokenMailBodyForActivateAccount(url, user.getUsername(), event.getPassword());
        }
        emailSender.sendHtmlEmail(Collections.singletonList(recipientAddress), subject, body,
                null, Collections.singletonList(supportMail));
    }

    /**
     * This will save a unique token for the public user and sends it as email link.
     *
     * @param event the details of email page and locale.
     */
    private void confirmPublicUserRegistration(OnRegistrationCompleteEvent event) {
        PublicUser publicUser = event.getPublicUser();
        String token = UUID.randomUUID().toString();
        publicUserService.createVerificationToken(publicUser, token);

        String recipientAddress = publicUser.getEmail();
        String subject = "Registration Confirmation";
        String confirmUrl = customGenerator.getPageUrlWithToken(event.getPageName(), token);

        emailSender.sendHtmlEmail(Collections.singletonList(recipientAddress), subject,
                emailSender.getVerifyTokenMailBody(confirmUrl), null, null);
    }
}
