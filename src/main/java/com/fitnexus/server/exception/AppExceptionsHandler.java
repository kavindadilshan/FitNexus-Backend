package com.fitnexus.server.exception;

import com.fitnexus.server.dto.exception.CustomAuthenticationException;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.exception.ErrorMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.Nonnull;



@ControllerAdvice
@Slf4j
public class AppExceptionsHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {CustomServiceException.class})
    public ResponseEntity<ErrorMessageResponse> handleServiceException(CustomServiceException ex, WebRequest webRequest) {
        ex.printStackTrace();
        log.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage(), ex.getCode()), HttpStatus.OK);
    }

    @ExceptionHandler(value = {CustomAuthenticationException.class})
    public ResponseEntity<ErrorMessageResponse> handleAuthenticationException(CustomAuthenticationException ex, WebRequest webRequest) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage(), HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {AccessDeniedException.class, CustomAccessDeniedException.class})
    public ResponseEntity<ErrorMessageResponse> handleAccessDeniedException(CustomAccessDeniedException ex, WebRequest webRequest) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage() != null ?
                ex.getMessage() : "Access denied", HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ResponseEntity<ErrorMessageResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest webRequest) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, "User not found", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity handleAllExceptions(MethodArgumentTypeMismatchException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleMissingServletRequestParameter(@Nonnull MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorMessageResponse(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }
}
