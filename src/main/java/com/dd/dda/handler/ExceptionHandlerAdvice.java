package com.dd.dda.handler;

import com.dd.dda.model.exception.DataNotFoundException;
import com.dd.dda.model.exception.DDAException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final String VALIDATION_ERROR = "Validation error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleException(MethodArgumentNotValidException e) {
        log.error(VALIDATION_ERROR, e);
        String errorsToString = validationErrorsToString(e);
        return ResponseEntity.badRequest().body(errorsToString);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleException(MethodArgumentTypeMismatchException e) {
        log.error(VALIDATION_ERROR, e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleException(MissingServletRequestParameterException e) {
        log.error(VALIDATION_ERROR, e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleException(ConstraintViolationException e) {
        log.error(VALIDATION_ERROR, e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleException(MultipartException e) {
        log.error("File upload exception", e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DDAException.class)
    public ResponseEntity<String> handleException(DDAException e) {
        log.error("Exception", e);
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<String> handleException(DataNotFoundException e) {
        log.error("Data in DB not found! {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleException(AccessDeniedException e) {
        log.error("Spring Security access Denied Exception", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sie haben kein recht dieses zu sehen / machen!");
    }

    private String validationErrorsToString(MethodArgumentNotValidException e) {
        final BindingResult bindingResult = e.getBindingResult();
        return bindingResult.getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("<br>"));
    }
}
