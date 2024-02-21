package com.fitnexus.server.controller;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.service.auth.UserService;
import com.fitnexus.server.constant.FitNexusConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;


@RequiredArgsConstructor
@Controller
public class TemplateTokenController {

    private enum UserType {
        AUTH, PUBLIC
    }

    private final UserService userService;
    private final PublicUserService publicUserService;

    @Value("${spring.mvc.servlet.path}")
    private String servletPath;

    @GetMapping("/error")
    public String error(Model model) {
        return "error"; //view
    }

    @GetMapping("/expired")
    public String expired(Model model) {
        return "expiredToken"; //view
    }

    @GetMapping("/register/success")
    public String regSuccess(Model model) {
        return "userRegSuccess"; //view
    }

    @GetMapping("/auth-user/register/confirm")
    public String confirmUserRegistration(WebRequest request, Model model, @RequestParam("token") String token) {
        HttpStatus httpStatus = userService.checkVerificationToken(token);
        return confirmRegistration(request, model, token, httpStatus, UserType.AUTH);
    }

    @GetMapping("/public-user/register/confirm")
    public String confirmPublicUserRegistration(WebRequest request, Model model, @RequestParam("token") String token) {
        HttpStatus httpStatus = publicUserService.checkVerificationToken(token);
        return confirmRegistration(request, model, token, httpStatus, UserType.PUBLIC);
    }

    @GetMapping("/auth-user/email/resend")
    public ResponseEntity resendAuthRegistrationToken(HttpServletRequest request, @RequestParam("token") String existingToken) {
        userService.resendEmailToken(existingToken);
        return ResponseEntity.ok(new CommonResponse<>(true, "New token has been sent!"));
    }

    @GetMapping("/public-user/email/resend")
    public ResponseEntity resendPublicRegistrationToken(HttpServletRequest request, @RequestParam("token") String existingToken) {
        publicUserService.resendEmailToken(existingToken);
        return ResponseEntity.ok(new CommonResponse<>(true, "New token has been sent!"));
    }


    private String confirmRegistration(WebRequest request, Model model, String token, HttpStatus httpStatus, UserType userType) {

        Locale locale = request.getLocale();

        switch (httpStatus) {
            case NOT_FOUND:
                model.addAttribute("message", "Invalid token");
                return error(model);
            case GONE:
                String path = servletPath.replace("/", "") + (userType == UserType.PUBLIC ? "/public-user" : "/auth-user");
                path = path + "/email/resend?token=";
                model.addAttribute("token", token);
                model.addAttribute("path", path);
                model.addAttribute("message", "Token is expired. please re-send");
                return expired(model);
            case OK:
                return "redirect:" + servletPath + "/register/success?lang=" + locale.getLanguage();
            default:
                model.addAttribute("message", FitNexusConstants.ErrorConstants.SERVICE_ERROR);
                return error(model);
        }

    }


}
