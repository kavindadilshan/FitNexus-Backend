package com.fitnexus.server.controller.admin.delete;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.auth.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/delete")
public class AdminDeleteController {

    private final AuthUserService authUserService;



    @DeleteMapping(value = "/coach/{username}")
    public ResponseEntity deleteAuthUser(@PathVariable(value = "username") String username) {
        log.info("\nDelete auth user: {}", username);
        authUserService.deleteAuthUser(username);
        return ResponseEntity.ok(new CommonResponse<>(true, "Auth User is deleted!"));
    }

}
