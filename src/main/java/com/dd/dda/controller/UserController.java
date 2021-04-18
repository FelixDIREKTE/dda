package com.dd.dda.controller;

import com.dd.dda.model.FileType;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.service.MailService;
import com.dd.dda.service.ParliamentService;
import com.dd.dda.service.UserService;
import com.dd.dda.service.file.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FileStorageService fileStorageService;
    private final MailService mailService;
    private final ParliamentService parliamentService;

    public UserController(BCryptPasswordEncoder bCryptPasswordEncoder, FileStorageService fileStorageService, MailService mailService, ParliamentService parliamentService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.fileStorageService = fileStorageService;
        this.mailService = mailService;
        this.parliamentService = parliamentService;
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long id) {
        if (id < 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        return ResponseEntity.ok(userService.getUserByIdEncrypted(id));
    }

    @GetMapping("/{email}/email")
    @PreAuthorize("hasAuthority('User') and #email.compareToIgnoreCase(principal.Username) == 0")
    public ResponseEntity<User> getUserByEmail(@PathVariable(value = "email") String email) {
        return ResponseEntity.ok(userService.getUserByMailWithoutPassword(email));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> updatePassword(@PathVariable(value = "id") Long id,
                                                  @RequestParam(value = "oldPassword") String oldPassword,
                                                  @RequestParam(value = "newPassword") String password) {
        if (id == null || id < 0 || oldPassword == null || oldPassword.isEmpty() || password == null || password.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }


        if (userService.updatePassword(id, oldPassword, password)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
    }


    @PostMapping("/forgotPassword")
    public ResponseEntity<Boolean> forgotPassword(@RequestParam(value = "email") String email) {
        if (email == null || email.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }

        userService.forgotPassword(email);
        return ResponseEntity.ok(true);

    }





    @PutMapping("/create")
    public ResponseEntity<Boolean> createUser(    @RequestParam(value = "email") String email,
                                                  @RequestParam(value = "password") String password) {

        userService.createUser(email, password);

        return ResponseEntity.ok(true);


    }

    @PutMapping("/{id}/verifymail")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> verifymail(   @PathVariable(value = "id") Long id,
                                                 @RequestParam(value = "ve") String ve) {
        return ResponseEntity.ok(userService.verifyMail(id, ve));
    }

    @PutMapping("/{id}/resendVE")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> resendVE(   @PathVariable(value = "id") Long id) {
        userService.resendVE(id);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/{id}/updateData")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> updateUserData(@PathVariable(value = "id") Long id,
                                                 @RequestParam(value = "name") String name,
                                                 @RequestParam(value = "firstname") String firstname,
                                                 @RequestParam(value = "street") String street,
                                                 @RequestParam(value = "housenr") String housenr,
                                                 @RequestParam(value = "zipcode") String zipcode,
                                                 @RequestParam(value = "birthdate") String birthdate
    ) {
        log.info("Enter into updateData");
        String result = userService.updateUserData(id, name, firstname, street, housenr, zipcode, birthdate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/updateCategories")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> updateCategories(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "categoryBits") int categoryBits
    ) {
        userService.updateCategories(id, categoryBits);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/{id}/getCategories")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Integer> getCategories(@PathVariable(value = "id") Long id
    ) {
        int result = userService.getCategories(id);
        return ResponseEntity.ok(result);
    }




    @PutMapping("/{id}/updateOptionalData")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> updateOptionalData(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "phonenr") String phonenr
    ) {
        log.info("Enter into updateOptionalData");
        String result = userService.updateUserOptionalData(id, phonenr);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/{id}/changeEmail")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> changeEmail(@PathVariable(value = "id") Long id,
                                                 @RequestParam(value = "email") String email
    ) {
        log.info("Enter into updateData");
        String result = userService.changeEmail(id, email);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("{id}/deleteSelf")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteSelf(@PathVariable(value = "id") Long id)  {

        fileStorageService.deleteAllFiles(FileType.USERVERIFICATION, id);
        fileStorageService.deleteAllFiles(FileType.USERPROFILEPIC, id);
        userService.deleteUser(id);
        return ResponseEntity.ok(true);
    }

}
