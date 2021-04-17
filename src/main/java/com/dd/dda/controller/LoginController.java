package com.dd.dda.controller;

import com.dd.dda.model.Login;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout", required = false) String logout, Model model, Principal principal, RedirectAttributes flash) {
        return ResponseEntity.ok(userService.login(error, model, principal, flash));
    }

    @PostMapping("/auth")
    @PreAuthorize("hasAuthority('User') AND #userLogin.email.compareToIgnoreCase(principal.Username) == 0")
    public ResponseEntity<User> authentication(@RequestBody @Valid Login userLogin) {
        return ResponseEntity.ok(userService.authenticate(userLogin.getEmail(), userLogin.getPassword()));
    }


    @GetMapping("{id}/isLoggedIn")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity isLoggedIn(@PathVariable(value = "id") Long id
    ) {
        return ResponseEntity.ok(true);
    }


    @GetMapping("/isLoggedIn")
    @PreAuthorize("hasAuthority('User')")
    public ResponseEntity isLoggedIn() {
        return ResponseEntity.ok(true);
    }





}
