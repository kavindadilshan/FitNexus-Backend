package com.fitnexus.server.util;

import com.fitnexus.server.entity.publicuser.PublicUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    @Value("${mail.from}")
    private final String mailFrom;

    @Value("${support_mail}")
    private String supportMail;

    private final JavaMailSender javaMailSender;

    /**
     * This is an Async method which can use to send emails.
     *
     * @param addresses the mail addresses array to send.
     * @param subject   the subject of the mail.
     * @param body      the email body string.
     */
    @Async
    public void sendEmail(List<String> addresses, String subject, String body) {
        log.info("\nNormal mail: Addresses: {} \nSubject: {}", addresses, subject);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(addresses.toArray(new String[0]));

        msg.setSubject(subject);
        msg.setText(body);

        javaMailSender.send(msg);

    }


    /**
     * This is an Async method which can use to send html emails.
     *
     * @param addresses  the mail addresses array to send.
     * @param subject    the subject of the mail.
     * @param htmlString the email body html string.
     */
    @Async
    public void sendHtmlEmail(List<String> addresses, String subject, String htmlString,
                              List<String> ccAddresses, List<String> bccAddresses) {

        if (bccAddresses == null || bccAddresses.isEmpty()) {
            bccAddresses = Collections.singletonList(supportMail);
        }

        List<String> verifiedAddresses = addresses != null
                ? addresses.stream().filter(this::isValidEmail).collect(Collectors.toList())
                : Collections.emptyList();

        if (verifiedAddresses.isEmpty()) {
            addresses = Collections.singletonList(supportMail);
            bccAddresses.remove(supportMail);
        } else {
            addresses = verifiedAddresses;
        }

        log.info("\nHTML mail: Addresses: {}\tcc: {}\tbcc: {} \nSubject: {}",
                addresses, ccAddresses, bccAddresses, subject);
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(addresses.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlString, true);
            helper.setFrom(new InternetAddress(mailFrom, "Fitzky"));

            if (ccAddresses != null && !ccAddresses.isEmpty()) {
                helper.setCc(ccAddresses.toArray(new String[0]));
            }
            if (bccAddresses != null && !bccAddresses.isEmpty()) {
                helper.setBcc(bccAddresses.toArray(new String[0]));
            }

            javaMailSender.send(mail);
            log.info("\nHTML mail send successfully to :{}\tcc: {}\tbcc: {}\t: Subject",
                    addresses, ccAddresses, bccAddresses, subject);
        } catch (MessagingException | UnsupportedEncodingException m) {
            log.error("Failed to send HTML email: " + m.getMessage(), m);
        }

    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.error("Invalid email - {}", email);
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        boolean matches = pattern.matcher(email).matches();
        if (!matches) {
            log.error("Invalid email - {}", email);
        }
        return matches;
    }

    public List<String> getPublicUserEmailList(PublicUser publicUser) {
        List<String> emails;
        if (publicUser.isEmailVerified()) {
            emails = Collections.singletonList(publicUser.getEmail());
        } else {
            emails = Collections.emptyList();
        }
        return emails;
    }

    /**
     * This cau use to get an html page which is used to send verification emails.
     *
     * @param urlInButton the url which needs to redirect the user to verify the token.
     * @return the html page as string.
     */
    public String getVerifyTokenMailBody(String urlInButton) {
        String message = "We received a request to set your email for fitzky. " +
                "If this is correct, please confirm by clicking the button below. If you don’t know why you got this email, " +
                "please tell us straight away so we can fix this for you.";
        String buttonName = "Confirm Email";
        return getBody(message, urlInButton, buttonName);
    }

    public String getVerifyTokenMailBodyForPasswordChange(int code) {
        String message = "We received a request to change your password for fitzky." +
                "Please use below verification code to proceed" +
                "<h4>" + code + "</h4>";
        return getBody(message);
    }

    public String getVerifyTokenMailBodyForActivateAccount(String urlInButton, String username, String password) {
        String message = "Welcome to Fitzky!\n" +
                "Your account has been created. Please use given credentials to activate your account." +
                "You can go through the mobile app or continue to the web app." +
                "<br>" +
                "<b>Your Username - " + username + "</b>" +
                "<br>" +
                "<b>Your First Login Password - " + password + "</b>";
        String buttonName = "Continue to Web App";
        return getBody(message, urlInButton, buttonName);
    }

    public String getLogInMailBodyForAlreadyActivaAccount(String urlInButton) {
        String message = "Welcome to Fitzky!\n" +
                "Your account has been created. Please use the existing credentials to activate your account." +
                "You can go through the mobile app or continue to the web app." +
                "<br>";
        String buttonName = "Continue to Web App";
        return getBody(message, urlInButton, buttonName);
    }

    public String getReservationSuccessHtml(String message, String urlInButton, String buttonName) {
        return getBody(message, urlInButton, buttonName);
    }

    public String getReservationSuccessHtml(String message) {
        return getBody(message);
    }

    public String getNotificationHtmlEmail(String message) {
        return getBody(message);
    }

    public String getEmailUpdatedNotifyHtml() {
        return getBody("You have successfully changed the registered mail address of your Fitzky business account");
    }

    public String getPasswordResetNotifyHtml(String urlInButton, String username, String password) {
        String message = "Your password has been reset. please use the below password to login " +
                "<br>" +
                "<b>Username - " + username + "</b>" +
                "<br>" +
                "<b>Your New Login Password - " + password + "</b>";
        String buttonName = "Continue to Web App";
        return getBody(message, urlInButton, buttonName);
    }

    public String getEmailVerifyHtml() {
        String message = "";
        String url = "";
        return getBody(message, url, "");
    }

    private String getBody(String message) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n" +
                "    <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta content=\"telephone=no\" name=\"format-detection\">\n" +
                "    <title></title>\n" +
                "    <!--[if (mso 16)]>\n" +
                "    <style type=\"text/css\">\n" +
                "        a {\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <![endif]-->\n" +
                "    <!--[if gte mso 9]>\n" +
                "    <style>sup {\n" +
                "        font-size: 100% !important;\n" +
                "    }</style><![endif]-->\n" +
                "    <style type=\"text/css\">\n" +
                "        /*\n" +
                "        CONFIG STYLES\n" +
                "        Please do not delete and edit CSS styles below\n" +
                "        */\n" +
                "        /* IMPORTANT THIS STYLES MUST BE ON FINAL EMAIL */\n" +
                "        #outlook a {\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "\n" +
                "        .ExternalClass {\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "\n" +
                "        .ExternalClass,\n" +
                "        .ExternalClass p,\n" +
                "        .ExternalClass span,\n" +
                "        .ExternalClass font,\n" +
                "        .ExternalClass td,\n" +
                "        .ExternalClass div {\n" +
                "            line-height: 100%;\n" +
                "        }\n" +
                "\n" +
                "        .es-button {\n" +
                "            mso-style-priority: 100 !important;\n" +
                "            text-decoration: none !important;\n" +
                "        }\n" +
                "\n" +
                "        a[x-apple-data-detectors] {\n" +
                "            color: inherit !important;\n" +
                "            text-decoration: none !important;\n" +
                "            font-size: inherit !important;\n" +
                "            font-family: inherit !important;\n" +
                "            font-weight: inherit !important;\n" +
                "            line-height: inherit !important;\n" +
                "        }\n" +
                "\n" +
                "        .es-desk-hidden {\n" +
                "            display: none;\n" +
                "            float: left;\n" +
                "            overflow: hidden;\n" +
                "            width: 0;\n" +
                "            max-height: 0;\n" +
                "            line-height: 0;\n" +
                "            mso-hide: all;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END OF IMPORTANT\n" +
                "        */\n" +
                "        s {\n" +
                "            text-decoration: line-through;\n" +
                "        }\n" +
                "\n" +
                "        html,\n" +
                "        body {\n" +
                "            width: 100%;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            -webkit-text-size-adjust: 100%;\n" +
                "            -ms-text-size-adjust: 100%;\n" +
                "        }\n" +
                "\n" +
                "        table {\n" +
                "            mso-table-lspace: 0pt;\n" +
                "            mso-table-rspace: 0pt;\n" +
                "            border-collapse: collapse;\n" +
                "            border-spacing: 0px;\n" +
                "        }\n" +
                "\n" +
                "        table td,\n" +
                "        html,\n" +
                "        body,\n" +
                "        .es-wrapper {\n" +
                "            padding: 0;\n" +
                "            Margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        .es-content,\n" +
                "        .es-header,\n" +
                "        .es-footer {\n" +
                "            table-layout: fixed !important;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "\n" +
                "        img {\n" +
                "            display: block;\n" +
                "            border: 0;\n" +
                "            outline: none;\n" +
                "            text-decoration: none;\n" +
                "            -ms-interpolation-mode: bicubic;\n" +
                "        }\n" +
                "\n" +
                "        table tr {\n" +
                "            border-collapse: collapse;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        hr {\n" +
                "            Margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        h1,\n" +
                "        h2,\n" +
                "        h3,\n" +
                "        h4,\n" +
                "        h5 {\n" +
                "            Margin: 0;\n" +
                "            line-height: 120%;\n" +
                "            mso-line-height-rule: exactly;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        ul li,\n" +
                "        ol li,\n" +
                "        a {\n" +
                "            -webkit-text-size-adjust: none;\n" +
                "            -ms-text-size-adjust: none;\n" +
                "            mso-line-height-rule: exactly;\n" +
                "        }\n" +
                "\n" +
                "        .es-left {\n" +
                "            float: left;\n" +
                "        }\n" +
                "\n" +
                "        .es-right {\n" +
                "            float: right;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5 {\n" +
                "            padding: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5t {\n" +
                "            padding-top: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5b {\n" +
                "            padding-bottom: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5l {\n" +
                "            padding-left: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5r {\n" +
                "            padding-right: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10 {\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10t {\n" +
                "            padding-top: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10b {\n" +
                "            padding-bottom: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10l {\n" +
                "            padding-left: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10r {\n" +
                "            padding-right: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15 {\n" +
                "            padding: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15t {\n" +
                "            padding-top: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15b {\n" +
                "            padding-bottom: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15l {\n" +
                "            padding-left: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15r {\n" +
                "            padding-right: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20 {\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20t {\n" +
                "            padding-top: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20b {\n" +
                "            padding-bottom: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20l {\n" +
                "            padding-left: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20r {\n" +
                "            padding-right: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25 {\n" +
                "            padding: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25t {\n" +
                "            padding-top: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25b {\n" +
                "            padding-bottom: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25l {\n" +
                "            padding-left: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25r {\n" +
                "            padding-right: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30 {\n" +
                "            padding: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30t {\n" +
                "            padding-top: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30b {\n" +
                "            padding-bottom: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30l {\n" +
                "            padding-left: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30r {\n" +
                "            padding-right: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35 {\n" +
                "            padding: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35t {\n" +
                "            padding-top: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35b {\n" +
                "            padding-bottom: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35l {\n" +
                "            padding-left: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35r {\n" +
                "            padding-right: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40 {\n" +
                "            padding: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40t {\n" +
                "            padding-top: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40b {\n" +
                "            padding-bottom: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40l {\n" +
                "            padding-left: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40r {\n" +
                "            padding-right: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td {\n" +
                "            border: 0;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td a img {\n" +
                "            display: inline-block !important;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END CONFIG STYLES\n" +
                "        */\n" +
                "        a {\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            font-size: 14px;\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "\n" +
                "        h1 {\n" +
                "            font-size: 30px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h1 a {\n" +
                "            font-size: 30px;\n" +
                "        }\n" +
                "\n" +
                "        h2 {\n" +
                "            font-size: 24px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h2 a {\n" +
                "            font-size: 24px;\n" +
                "        }\n" +
                "\n" +
                "        h3 {\n" +
                "            font-size: 20px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h3 a {\n" +
                "            font-size: 20px;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "            font-size: 14px;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            line-height: 150%;\n" +
                "        }\n" +
                "\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "            Margin-bottom: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td a {\n" +
                "            text-decoration: none;\n" +
                "            display: block;\n" +
                "        }\n" +
                "\n" +
                "        .es-wrapper {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            background-image: ;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-wrapper-color {\n" +
                "            background-color: #ffffff;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body {\n" +
                "            background-color: #ffffff;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body p,\n" +
                "        .es-content-body ul li,\n" +
                "        .es-content-body ol li {\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body a {\n" +
                "            color: #ee6c6d;\n" +
                "        }\n" +
                "\n" +
                "        .es-header {\n" +
                "            background-color: transparent;\n" +
                "            background-image: ;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body {\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body p,\n" +
                "        .es-header-body ul li,\n" +
                "        .es-header-body ol li {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body a {\n" +
                "            color: #ee6c6d;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer {\n" +
                "            background-color: transparent;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body {\n" +
                "            background-color: #f7f7f7;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body p,\n" +
                "        .es-footer-body ul li,\n" +
                "        .es-footer-body ol li {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body a {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-infoblock,\n" +
                "        .es-infoblock p,\n" +
                "        .es-infoblock ul li,\n" +
                "        .es-infoblock ol li {\n" +
                "            line-height: 120%;\n" +
                "            font-size: 12px;\n" +
                "            color: #cccccc;\n" +
                "        }\n" +
                "\n" +
                "        .es-infoblock a {\n" +
                "            font-size: 12px;\n" +
                "            color: #cccccc;\n" +
                "        }\n" +
                "\n" +
                "        a.es-button {\n" +
                "            border-style: solid;\n" +
                "            border-color: #474745;\n" +
                "            border-width: 6px 25px 6px 25px;\n" +
                "            display: inline-block;\n" +
                "            background: #474745;\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 16px;\n" +
                "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n" +
                "            font-weight: normal;\n" +
                "            font-style: normal;\n" +
                "            line-height: 120%;\n" +
                "            color: #efefef;\n" +
                "            text-decoration: none;\n" +
                "            width: auto;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        .es-button-border {\n" +
                "            border-style: solid solid solid solid;\n" +
                "            border-color: #474745 #474745 #474745 #474745;\n" +
                "            background: #474745;\n" +
                "            border-width: 0px 0px 0px 0px;\n" +
                "            display: inline-block;\n" +
                "            border-radius: 20px;\n" +
                "            width: auto;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        RESPONSIVE STYLES\n" +
                "        Please do not delete and edit CSS styles below.\n" +
                "\n" +
                "        If you don't need responsive layout, please delete this section.\n" +
                "        */\n" +
                "        @media only screen and (max-width: 600px) {\n" +
                "\n" +
                "            p,\n" +
                "            ul li,\n" +
                "            ol li,\n" +
                "            a {\n" +
                "                font-size: 16px !important;\n" +
                "                line-height: 150% !important;\n" +
                "            }\n" +
                "\n" +
                "            h1 {\n" +
                "                font-size: 30px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h2 {\n" +
                "                font-size: 26px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h3 {\n" +
                "                font-size: 20px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h1 a {\n" +
                "                font-size: 30px !important;\n" +
                "            }\n" +
                "\n" +
                "            h2 a {\n" +
                "                font-size: 26px !important;\n" +
                "            }\n" +
                "\n" +
                "            h3 a {\n" +
                "                font-size: 20px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-menu td a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-header-body p,\n" +
                "            .es-header-body ul li,\n" +
                "            .es-header-body ol li,\n" +
                "            .es-header-body a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-footer-body p,\n" +
                "            .es-footer-body ul li,\n" +
                "            .es-footer-body ol li,\n" +
                "            .es-footer-body a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-infoblock p,\n" +
                "            .es-infoblock ul li,\n" +
                "            .es-infoblock ol li,\n" +
                "            .es-infoblock a {\n" +
                "                font-size: 12px !important;\n" +
                "            }\n" +
                "\n" +
                "            *[class=\"gmail-fix\"] {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-c,\n" +
                "            .es-m-txt-c h1,\n" +
                "            .es-m-txt-c h2,\n" +
                "            .es-m-txt-c h3 {\n" +
                "                text-align: center !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-r,\n" +
                "            .es-m-txt-r h1,\n" +
                "            .es-m-txt-r h2,\n" +
                "            .es-m-txt-r h3 {\n" +
                "                text-align: right !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-l,\n" +
                "            .es-m-txt-l h1,\n" +
                "            .es-m-txt-l h2,\n" +
                "            .es-m-txt-l h3 {\n" +
                "                text-align: left !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-r img,\n" +
                "            .es-m-txt-c img,\n" +
                "            .es-m-txt-l img {\n" +
                "                display: inline !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-button-border {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "\n" +
                "            a.es-button {\n" +
                "                font-size: 20px !important;\n" +
                "                display: inline-block !important;\n" +
                "                border-width: 6px 25px 6px 25px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-btn-fw {\n" +
                "                border-width: 10px 0px !important;\n" +
                "                text-align: center !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-adaptive table,\n" +
                "            .es-btn-fw,\n" +
                "            .es-btn-fw-brdr,\n" +
                "            .es-left,\n" +
                "            .es-right {\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-content table,\n" +
                "            .es-header table,\n" +
                "            .es-footer table,\n" +
                "            .es-content,\n" +
                "            .es-footer,\n" +
                "            .es-header {\n" +
                "                width: 100% !important;\n" +
                "                max-width: 600px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-adapt-td {\n" +
                "                display: block !important;\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            .adapt-img {\n" +
                "                width: 100% !important;\n" +
                "                height: auto !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0 {\n" +
                "                padding: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0r {\n" +
                "                padding-right: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0l {\n" +
                "                padding-left: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0t {\n" +
                "                padding-top: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0b {\n" +
                "                padding-bottom: 0 !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p20b {\n" +
                "                padding-bottom: 20px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-mobile-hidden,\n" +
                "            .es-hidden {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-desk-hidden {\n" +
                "                display: table-row !important;\n" +
                "                width: auto !important;\n" +
                "                overflow: visible !important;\n" +
                "                float: none !important;\n" +
                "                max-height: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-desk-menu-hidden {\n" +
                "                display: table-cell !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-table-not-adapt,\n" +
                "            .esd-block-html table {\n" +
                "                width: auto !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-social {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-social td {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END RESPONSIVE STYLES\n" +
                "        */\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<div class=\"es-wrapper-color\">\n" +
                "    <!--[if gte mso 9]>\n" +
                "    <v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n" +
                "        <v:fill type=\"tile\" color=\"#ffffff\"></v:fill>\n" +
                "    </v:background>\n" +
                "    <![endif]-->\n" +
                "    <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "        <tbody>\n" +
                "        <tr>\n" +
                "            <td class=\"esd-email-paddings\" valign=\"top\" align=\"center\">\n" +
                "                <table class=\"es-header esd-header-popover\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"es-adaptive esd-stripe\" esd-custom-block-id=\"8429\" align=\"center\">\n" +
                "                            <table class=\"es-header-body\" align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p25t es-p10b es-p20r es-p20l\" align=\"left\">\n" +
                "                                        <!--[if mso]>\n" +
                "                                        <table width=\"560\" cellpadding=\"0\"\n" +
                "                                               cellspacing=\"0\">\n" +
                "                                            <tr>\n" +
                "                                                <td width=\"203\" valign=\"top\"><![endif]-->\n" +
                "                                        <table class=\"es-left\" align=\"left\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"es-m-p0r es-m-p20b esd-container-frame\" align=\"center\"\n" +
                "                                                    width=\"203\" valign=\"top\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-image es-m-p0l es-m-txt-c\"\n" +
                "                                                                align=\"right\" style=\"font-size:0\"><a\n" +
                "                                                                    href=\"https://viewstripo.email/\"\n" +
                "                                                                    target=\"_blank\"><img\n" +
                "                                                                    src=\"https://d3iitm8eqnsqba.cloudfront.net/fitzky-logo.jpg\"\n" +
                "                                                                    alt=\"Logo\" style=\"display: block;\" title=\"Logo\"\n" +
                "                                                                    width=\"178\"></a></td>\n" +
                "                                                        </tr>\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                        <!--[if mso]></td>\n" +
                "                                    <td width=\"10\"></td>\n" +
                "                                    <td width=\"347\" valign=\"top\"><![endif]-->\n" +
                "                                        <table align=\"left\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"esd-container-frame\" align=\"left\" width=\"347\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td align=\"left\" class=\"esd-block-text es-m-txt-c es-p15\">\n" +
                "                                                                <p style=\"font-size: 16px;\">Fitzky Fitness Service</p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                        <!--[if mso]></td></tr></table><![endif]-->\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table class=\"es-content\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"esd-stripe\" align=\"center\">\n" +
                "                            <table class=\"es-content-body\"\n" +
                "                                   style=\"border-left:1px solid transparent;border-right:1px solid transparent;border-top:1px solid transparent;border-bottom:1px solid transparent;\"\n" +
                "                                   align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p20t es-p40b es-p40r es-p40l\" esd-custom-block-id=\"8537\"\n" +
                "                                        align=\"left\">\n" +
                "                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"esd-container-frame\" align=\"left\" width=\"518\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-image es-m-txt-c es-p5b\" align=\"center\"\n" +
                "                                                                style=\"font-size:0\"><a target=\"_blank\"><img\n" +
                "                                                                    src=\"https://d3iitm8eqnsqba.cloudfront.net/fitzky-logo.jpg\"\n" +
                "                                                                    alt=\"icon\" style=\"display: block;\" title=\"icon\"\n" +
                "                                                                    width=\"30\"></a></td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-m-txt-c\" align=\"center\">\n" +
                "                                                                <h2>Hey there!<br></h2>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-m-txt-c es-p15t\"\n" +
                "                                                                align=\"center\">\n" +
                "                                                                <p>" + message + "</p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <br><br>\n" +
                "                <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-footer\" align=\"center\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"esd-stripe\" esd-custom-block-id=\"8442\" style=\"background-color: #f7f7f7;\"\n" +
                "                            bgcolor=\"#f7f7f7\" align=\"center\">\n" +
                "                            <table class=\"es-footer-body\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p20t es-p20b es-p20r es-p20l\"\n" +
                "                                        esd-general-paddings-checked=\"false\" align=\"left\">\n" +
                "                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"esd-container-frame\" width=\"560\" valign=\"top\" align=\"center\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p5b\" align=\"center\">\n" +
                "                                                                <h3 style=\"line-height: 150%;\">Let's get social</h3>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-social es-p10t es-p10b\" align=\"center\"\n" +
                "                                                                style=\"font-size:0\">\n" +
                "                                                                <table class=\"es-table-not-adapt es-social\"\n" +
                "                                                                       cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                    <tbody>\n" +
                "                                                                    <tr>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Facebook\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/facebook-logo-black.png\"\n" +
                "                                                                                    alt=\"Fb\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Youtube\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/youtube-logo-black.png\"\n" +
                "                                                                                    alt=\"Yt\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Pinterest\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/pinterest-logo-black.png\"\n" +
                "                                                                                    alt=\"P\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"\n" +
                "                                                                               target=\"_blank\"><img title=\"Instagram\"\n" +
                "                                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/instagram-logo-black.png\"\n" +
                "                                                                                                    alt=\"Ig\" width=\"32\"\n" +
                "                                                                                                    height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td valign=\"top\" align=\"center\"><a\n" +
                "                                                                                href=\"#\"\n" +
                "                                                                                target=\"_blank\"><img title=\"Twitter\"\n" +
                "                                                                                                     src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/twitter-logo-black.png\"\n" +
                "                                                                                                     alt=\"Tw\" width=\"32\"\n" +
                "                                                                                                     height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                    </tr>\n" +
                "                                                                    </tbody>\n" +
                "                                                                </table>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td align=\"center\" class=\"esd-block-text es-p10t es-p10b\">\n" +
                "                                                                <p style=\"line-height: 150%;\">You are receiving this\n" +
                "                                                                    email because you have successfully registered for the fitzky fitness services.</p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p10b\" align=\"center\">\n" +
                "                                                                <p style=\"line-height: 150%;\"><strong>Fitzky\n" +
                "                                                                    designed by <a target=\"_blank\"\n" +
                "                                                                                   href=\"http://www.ceyentra.com/\">www.ceyentra.com</a>.</strong>\n" +
                "                                                                </p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p10t es-p10b\" align=\"center\">\n" +
                "                                                                <p>© 2020<br></p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</div>\n" +
                "<div style=\"position: absolute; left: -9999px; top: -9999px; margin: 0px;\"></div>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }

    private String getBody(String message, String urlInButton, String buttonText) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n" +
                "    <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta content=\"telephone=no\" name=\"format-detection\">\n" +
                "    <title></title>\n" +
                "    <!--[if (mso 16)]>\n" +
                "    <style type=\"text/css\">\n" +
                "        a {\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <![endif]-->\n" +
                "    <!--[if gte mso 9]>\n" +
                "    <style>sup {\n" +
                "        font-size: 100% !important;\n" +
                "    }</style><![endif]-->\n" +
                "    <style type=\"text/css\">\n" +
                "        /*\n" +
                "        CONFIG STYLES\n" +
                "        Please do not delete and edit CSS styles below\n" +
                "        */\n" +
                "        /* IMPORTANT THIS STYLES MUST BE ON FINAL EMAIL */\n" +
                "        #outlook a {\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "\n" +
                "        .ExternalClass {\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "\n" +
                "        .ExternalClass,\n" +
                "        .ExternalClass p,\n" +
                "        .ExternalClass span,\n" +
                "        .ExternalClass font,\n" +
                "        .ExternalClass td,\n" +
                "        .ExternalClass div {\n" +
                "            line-height: 100%;\n" +
                "        }\n" +
                "\n" +
                "        .es-button {\n" +
                "            mso-style-priority: 100 !important;\n" +
                "            text-decoration: none !important;\n" +
                "        }\n" +
                "\n" +
                "        a[x-apple-data-detectors] {\n" +
                "            color: inherit !important;\n" +
                "            text-decoration: none !important;\n" +
                "            font-size: inherit !important;\n" +
                "            font-family: inherit !important;\n" +
                "            font-weight: inherit !important;\n" +
                "            line-height: inherit !important;\n" +
                "        }\n" +
                "\n" +
                "        .es-desk-hidden {\n" +
                "            display: none;\n" +
                "            float: left;\n" +
                "            overflow: hidden;\n" +
                "            width: 0;\n" +
                "            max-height: 0;\n" +
                "            line-height: 0;\n" +
                "            mso-hide: all;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END OF IMPORTANT\n" +
                "        */\n" +
                "        s {\n" +
                "            text-decoration: line-through;\n" +
                "        }\n" +
                "\n" +
                "        html,\n" +
                "        body {\n" +
                "            width: 100%;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            -webkit-text-size-adjust: 100%;\n" +
                "            -ms-text-size-adjust: 100%;\n" +
                "        }\n" +
                "\n" +
                "        table {\n" +
                "            mso-table-lspace: 0pt;\n" +
                "            mso-table-rspace: 0pt;\n" +
                "            border-collapse: collapse;\n" +
                "            border-spacing: 0px;\n" +
                "        }\n" +
                "\n" +
                "        table td,\n" +
                "        html,\n" +
                "        body,\n" +
                "        .es-wrapper {\n" +
                "            padding: 0;\n" +
                "            Margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        .es-content,\n" +
                "        .es-header,\n" +
                "        .es-footer {\n" +
                "            table-layout: fixed !important;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "\n" +
                "        img {\n" +
                "            display: block;\n" +
                "            border: 0;\n" +
                "            outline: none;\n" +
                "            text-decoration: none;\n" +
                "            -ms-interpolation-mode: bicubic;\n" +
                "        }\n" +
                "\n" +
                "        table tr {\n" +
                "            border-collapse: collapse;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        hr {\n" +
                "            Margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        h1,\n" +
                "        h2,\n" +
                "        h3,\n" +
                "        h4,\n" +
                "        h5 {\n" +
                "            Margin: 0;\n" +
                "            line-height: 120%;\n" +
                "            mso-line-height-rule: exactly;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        ul li,\n" +
                "        ol li,\n" +
                "        a {\n" +
                "            -webkit-text-size-adjust: none;\n" +
                "            -ms-text-size-adjust: none;\n" +
                "            mso-line-height-rule: exactly;\n" +
                "        }\n" +
                "\n" +
                "        .es-left {\n" +
                "            float: left;\n" +
                "        }\n" +
                "\n" +
                "        .es-right {\n" +
                "            float: right;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5 {\n" +
                "            padding: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5t {\n" +
                "            padding-top: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5b {\n" +
                "            padding-bottom: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5l {\n" +
                "            padding-left: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p5r {\n" +
                "            padding-right: 5px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10 {\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10t {\n" +
                "            padding-top: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10b {\n" +
                "            padding-bottom: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10l {\n" +
                "            padding-left: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p10r {\n" +
                "            padding-right: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15 {\n" +
                "            padding: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15t {\n" +
                "            padding-top: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15b {\n" +
                "            padding-bottom: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15l {\n" +
                "            padding-left: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p15r {\n" +
                "            padding-right: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20 {\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20t {\n" +
                "            padding-top: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20b {\n" +
                "            padding-bottom: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20l {\n" +
                "            padding-left: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p20r {\n" +
                "            padding-right: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25 {\n" +
                "            padding: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25t {\n" +
                "            padding-top: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25b {\n" +
                "            padding-bottom: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25l {\n" +
                "            padding-left: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p25r {\n" +
                "            padding-right: 25px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30 {\n" +
                "            padding: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30t {\n" +
                "            padding-top: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30b {\n" +
                "            padding-bottom: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30l {\n" +
                "            padding-left: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p30r {\n" +
                "            padding-right: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35 {\n" +
                "            padding: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35t {\n" +
                "            padding-top: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35b {\n" +
                "            padding-bottom: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35l {\n" +
                "            padding-left: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p35r {\n" +
                "            padding-right: 35px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40 {\n" +
                "            padding: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40t {\n" +
                "            padding-top: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40b {\n" +
                "            padding-bottom: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40l {\n" +
                "            padding-left: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-p40r {\n" +
                "            padding-right: 40px;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td {\n" +
                "            border: 0;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td a img {\n" +
                "            display: inline-block !important;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END CONFIG STYLES\n" +
                "        */\n" +
                "        a {\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            font-size: 14px;\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "\n" +
                "        h1 {\n" +
                "            font-size: 30px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h1 a {\n" +
                "            font-size: 30px;\n" +
                "        }\n" +
                "\n" +
                "        h2 {\n" +
                "            font-size: 24px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h2 a {\n" +
                "            font-size: 24px;\n" +
                "        }\n" +
                "\n" +
                "        h3 {\n" +
                "            font-size: 20px;\n" +
                "            font-style: normal;\n" +
                "            font-weight: normal;\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        h3 a {\n" +
                "            font-size: 20px;\n" +
                "        }\n" +
                "\n" +
                "        p,\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "            font-size: 14px;\n" +
                "            font-family: arial, 'helvetica neue', helvetica, sans-serif;\n" +
                "            line-height: 150%;\n" +
                "        }\n" +
                "\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "            Margin-bottom: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .es-menu td a {\n" +
                "            text-decoration: none;\n" +
                "            display: block;\n" +
                "        }\n" +
                "\n" +
                "        .es-wrapper {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            background-image: ;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-wrapper-color {\n" +
                "            background-color: #ffffff;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body {\n" +
                "            background-color: #ffffff;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body p,\n" +
                "        .es-content-body ul li,\n" +
                "        .es-content-body ol li {\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "\n" +
                "        .es-content-body a {\n" +
                "            color: #ee6c6d;\n" +
                "        }\n" +
                "\n" +
                "        .es-header {\n" +
                "            background-color: transparent;\n" +
                "            background-image: ;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body {\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body p,\n" +
                "        .es-header-body ul li,\n" +
                "        .es-header-body ol li {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-header-body a {\n" +
                "            color: #ee6c6d;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer {\n" +
                "            background-color: transparent;\n" +
                "            background-repeat: repeat;\n" +
                "            background-position: center top;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body {\n" +
                "            background-color: #f7f7f7;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body p,\n" +
                "        .es-footer-body ul li,\n" +
                "        .es-footer-body ol li {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-footer-body a {\n" +
                "            color: #333333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .es-infoblock,\n" +
                "        .es-infoblock p,\n" +
                "        .es-infoblock ul li,\n" +
                "        .es-infoblock ol li {\n" +
                "            line-height: 120%;\n" +
                "            font-size: 12px;\n" +
                "            color: #cccccc;\n" +
                "        }\n" +
                "\n" +
                "        .es-infoblock a {\n" +
                "            font-size: 12px;\n" +
                "            color: #cccccc;\n" +
                "        }\n" +
                "\n" +
                "        a.es-button {\n" +
                "            border-style: solid;\n" +
                "            border-color: #474745;\n" +
                "            border-width: 6px 25px 6px 25px;\n" +
                "            display: inline-block;\n" +
                "            background: #474745;\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 16px;\n" +
                "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n" +
                "            font-weight: normal;\n" +
                "            font-style: normal;\n" +
                "            line-height: 120%;\n" +
                "            color: #efefef;\n" +
                "            text-decoration: none;\n" +
                "            width: auto;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        .es-button-border {\n" +
                "            border-style: solid solid solid solid;\n" +
                "            border-color: #474745 #474745 #474745 #474745;\n" +
                "            background: #474745;\n" +
                "            border-width: 0px 0px 0px 0px;\n" +
                "            display: inline-block;\n" +
                "            border-radius: 20px;\n" +
                "            width: auto;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        RESPONSIVE STYLES\n" +
                "        Please do not delete and edit CSS styles below.\n" +
                "\n" +
                "        If you don't need responsive layout, please delete this section.\n" +
                "        */\n" +
                "        @media only screen and (max-width: 600px) {\n" +
                "\n" +
                "            p,\n" +
                "            ul li,\n" +
                "            ol li,\n" +
                "            a {\n" +
                "                font-size: 16px !important;\n" +
                "                line-height: 150% !important;\n" +
                "            }\n" +
                "\n" +
                "            h1 {\n" +
                "                font-size: 30px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h2 {\n" +
                "                font-size: 26px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h3 {\n" +
                "                font-size: 20px !important;\n" +
                "                text-align: center;\n" +
                "                line-height: 120% !important;\n" +
                "            }\n" +
                "\n" +
                "            h1 a {\n" +
                "                font-size: 30px !important;\n" +
                "            }\n" +
                "\n" +
                "            h2 a {\n" +
                "                font-size: 26px !important;\n" +
                "            }\n" +
                "\n" +
                "            h3 a {\n" +
                "                font-size: 20px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-menu td a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-header-body p,\n" +
                "            .es-header-body ul li,\n" +
                "            .es-header-body ol li,\n" +
                "            .es-header-body a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-footer-body p,\n" +
                "            .es-footer-body ul li,\n" +
                "            .es-footer-body ol li,\n" +
                "            .es-footer-body a {\n" +
                "                font-size: 16px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-infoblock p,\n" +
                "            .es-infoblock ul li,\n" +
                "            .es-infoblock ol li,\n" +
                "            .es-infoblock a {\n" +
                "                font-size: 12px !important;\n" +
                "            }\n" +
                "\n" +
                "            *[class=\"gmail-fix\"] {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-c,\n" +
                "            .es-m-txt-c h1,\n" +
                "            .es-m-txt-c h2,\n" +
                "            .es-m-txt-c h3 {\n" +
                "                text-align: center !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-r,\n" +
                "            .es-m-txt-r h1,\n" +
                "            .es-m-txt-r h2,\n" +
                "            .es-m-txt-r h3 {\n" +
                "                text-align: right !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-l,\n" +
                "            .es-m-txt-l h1,\n" +
                "            .es-m-txt-l h2,\n" +
                "            .es-m-txt-l h3 {\n" +
                "                text-align: left !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-txt-r img,\n" +
                "            .es-m-txt-c img,\n" +
                "            .es-m-txt-l img {\n" +
                "                display: inline !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-button-border {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "\n" +
                "            a.es-button {\n" +
                "                font-size: 20px !important;\n" +
                "                display: inline-block !important;\n" +
                "                border-width: 6px 25px 6px 25px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-btn-fw {\n" +
                "                border-width: 10px 0px !important;\n" +
                "                text-align: center !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-adaptive table,\n" +
                "            .es-btn-fw,\n" +
                "            .es-btn-fw-brdr,\n" +
                "            .es-left,\n" +
                "            .es-right {\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-content table,\n" +
                "            .es-header table,\n" +
                "            .es-footer table,\n" +
                "            .es-content,\n" +
                "            .es-footer,\n" +
                "            .es-header {\n" +
                "                width: 100% !important;\n" +
                "                max-width: 600px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-adapt-td {\n" +
                "                display: block !important;\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            .adapt-img {\n" +
                "                width: 100% !important;\n" +
                "                height: auto !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0 {\n" +
                "                padding: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0r {\n" +
                "                padding-right: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0l {\n" +
                "                padding-left: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0t {\n" +
                "                padding-top: 0px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p0b {\n" +
                "                padding-bottom: 0 !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-m-p20b {\n" +
                "                padding-bottom: 20px !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-mobile-hidden,\n" +
                "            .es-hidden {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-desk-hidden {\n" +
                "                display: table-row !important;\n" +
                "                width: auto !important;\n" +
                "                overflow: visible !important;\n" +
                "                float: none !important;\n" +
                "                max-height: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            .es-desk-menu-hidden {\n" +
                "                display: table-cell !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-table-not-adapt,\n" +
                "            .esd-block-html table {\n" +
                "                width: auto !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-social {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "\n" +
                "            table.es-social td {\n" +
                "                display: inline-block !important;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "        END RESPONSIVE STYLES\n" +
                "        */\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<div class=\"es-wrapper-color\">\n" +
                "    <!--[if gte mso 9]>\n" +
                "    <v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n" +
                "        <v:fill type=\"tile\" color=\"#ffffff\"></v:fill>\n" +
                "    </v:background>\n" +
                "    <![endif]-->\n" +
                "    <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "        <tbody>\n" +
                "        <tr>\n" +
                "            <td class=\"esd-email-paddings\" valign=\"top\" align=\"center\">\n" +
                "                <table class=\"es-header esd-header-popover\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"es-adaptive esd-stripe\" esd-custom-block-id=\"8429\" align=\"center\">\n" +
                "                            <table class=\"es-header-body\" align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p25t es-p10b es-p20r es-p20l\" align=\"left\">\n" +
                "                                        <!--[if mso]>\n" +
                "                                        <table width=\"560\" cellpadding=\"0\"\n" +
                "                                               cellspacing=\"0\">\n" +
                "                                            <tr>\n" +
                "                                                <td width=\"203\" valign=\"top\"><![endif]-->\n" +
                "                                        <table class=\"es-content\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"es-m-p0r es-m-p20b esd-container-frame\" align=\"center\"\n" +
                "                                                    width=\"203\" valign=\"top\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-image es-m-p0l es-m-txt-c\"\n" +
                "                                                                align=\"center\" style=\"font-size:0\"><a\n" +
                "                                                                    href=\"https://viewstripo.email/\"\n" +
                "                                                                    target=\"_blank\"><img\n" +
                "                                                                    src=\"https://d3iitm8eqnsqba.cloudfront.net/fitzky-logo.jpg\"\n" +
                "                                                                    alt=\"Logo\" style=\"display: block;\" title=\"Logo\"\n" +
                "                                                                    width=\"178\"></a></td>\n" +
                "                                                        </tr>\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table class=\"es-content\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"esd-stripe\" align=\"center\">\n" +
                "                            <table class=\"es-content-body\"\n" +
                "                                   style=\"border-left:1px solid transparent;border-right:1px solid transparent;border-top:1px solid transparent;border-bottom:1px solid transparent;\"\n" +
                "                                   align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p20t es-p40b es-p40r es-p40l\" esd-custom-block-id=\"8537\"\n" +
                "                                        align=\"left\">\n" +
                "                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"esd-container-frame\" align=\"left\" width=\"518\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-m-txt-c\" align=\"center\">\n" +
                "                                                                <h2>Thank you for purchasing fitzky services!<br></h2>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-m-txt-c es-p15t\"\n" +
                "                                                                align=\"center\">\n" +
                "                                                                <p> " + message + " </p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <div >\n" +
                "                    <a class=\"btn\">\n" +
                "                        <span class=\"es-button-border\"><a href=\" " + urlInButton + " \"\n" +
                "                           style=\"font-size: 26px;\n" +
                "                                border-style: solid;\n" +
                "                                border-color: #474745;\n" +
                "                                border-width: 6px 25px 6px 25px;\n" +
                "                                display: inline-block;\n" +
                "                                background: #474745;\n" +
                "                                border-radius: 20px;\n" +
                "                                font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n" +
                "                                font-weight: normal;\n" +
                "                                font-style: normal;\n" +
                "                                line-height: 120%;\n" +
                "                                color: #efefef;\n" +
                "                                text-decoration: none;\n" +
                "                                width: auto;\n" +
                "                                text-align: center;\"> " + buttonText + " </a></span>\n" +
                "                    </a>\n" +
                "                </div>\n" +
                "                <br><br>\n" +
                "                <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-footer\" align=\"center\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td class=\"esd-stripe\" esd-custom-block-id=\"8442\" style=\"background-color: #f7f7f7;\"\n" +
                "                            bgcolor=\"#f7f7f7\" align=\"center\">\n" +
                "                            <table class=\"es-footer-body\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-structure es-p20t es-p20b es-p20r es-p20l\"\n" +
                "                                        esd-general-paddings-checked=\"false\" align=\"left\">\n" +
                "                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                            <tbody>\n" +
                "                                            <tr>\n" +
                "                                                <td class=\"esd-container-frame\" width=\"560\" valign=\"top\" align=\"center\">\n" +
                "                                                    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                        <tbody>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p5b\" align=\"center\">\n" +
                "                                                                <h3 style=\"line-height: 150%;\">Let's get social</h3>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-social es-p10t es-p10b\" align=\"center\"\n" +
                "                                                                style=\"font-size:0\">\n" +
                "                                                                <table class=\"es-table-not-adapt es-social\"\n" +
                "                                                                       cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                    <tbody>\n" +
                "                                                                    <tr>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Facebook\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/facebook-logo-black.png\"\n" +
                "                                                                                    alt=\"Fb\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Youtube\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/youtube-logo-black.png\"\n" +
                "                                                                                    alt=\"Yt\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"><img\n" +
                "                                                                                    title=\"Pinterest\"\n" +
                "                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/pinterest-logo-black.png\"\n" +
                "                                                                                    alt=\"P\" width=\"32\" height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td class=\"es-p20r\" valign=\"top\" align=\"center\">\n" +
                "                                                                            <a href=\"#\"\n" +
                "                                                                               target=\"_blank\"><img title=\"Instagram\"\n" +
                "                                                                                                    src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/instagram-logo-black.png\"\n" +
                "                                                                                                    alt=\"Ig\" width=\"32\"\n" +
                "                                                                                                    height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                        <td valign=\"top\" align=\"center\"><a\n" +
                "                                                                                href=\"#\"\n" +
                "                                                                                target=\"_blank\"><img title=\"Twitter\"\n" +
                "                                                                                                     src=\"https://ibicfz.stripocdn.email/content/assets/img/social-icons/logo-black/twitter-logo-black.png\"\n" +
                "                                                                                                     alt=\"Tw\" width=\"32\"\n" +
                "                                                                                                     height=\"32\"></a>\n" +
                "                                                                        </td>\n" +
                "                                                                    </tr>\n" +
                "                                                                    </tbody>\n" +
                "                                                                </table>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td align=\"center\" class=\"esd-block-text es-p10t es-p10b\">\n" +
                "                                                                <p style=\"line-height: 150%;\">You are receiving this\n" +
                "                                                                    email because you have successfully registered for the fitzky fitness services.</p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p10b\" align=\"center\">\n" +
                "                                                                <p style=\"line-height: 150%;\"><strong>Fitzky\n" +
                "                                                                    designed by <a target=\"_blank\"\n" +
                "                                                                                   href=\"http://www.ceyentra.com/\">www.ceyentra.com</a>.</strong>\n" +
                "                                                                </p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        <tr>\n" +
                "                                                            <td class=\"esd-block-text es-p10t es-p10b\" align=\"center\">\n" +
                "                                                                <p>© 2020<br></p>\n" +
                "                                                            </td>\n" +
                "                                                        </tr>\n" +
                "                                                        </tbody>\n" +
                "                                                    </table>\n" +
                "                                                </td>\n" +
                "                                            </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</div>\n" +
                "<div style=\"position: absolute; left: -9999px; top: -9999px; margin: 0px;\"></div>\n" +
                "</body>\n" +
                "\n" +
                "</html>\n";
    }
}
