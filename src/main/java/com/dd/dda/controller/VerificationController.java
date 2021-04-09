package com.dd.dda.controller;

import com.dd.dda.model.VerificationData;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.service.UserService;
import com.dd.dda.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/verification")
public class VerificationController {

    private final VerificationService verificationService;
    private final UserService userService;

    public VerificationController(VerificationService verificationService, UserService userService) {
        this.verificationService = verificationService;
        this.userService = userService;
    }

    @GetMapping("/{id}/getWatingUser")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<VerificationData> getWaitingUser(@PathVariable(value = "id") Long id) {

        if (id < 0) {
            throw new DDAException("Negative Ids are not allowed");
        }
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }

        return ResponseEntity.ok(verificationService.getWaitingUser());
    }


    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> verify(@PathVariable(value = "id") Long id,
                                                  @RequestParam(value = "verifiedId") Long verifiedId,
                                          @RequestParam(value = "verify") boolean verify,
                                          @RequestParam(value = "msg") String msg

    ) {
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }
        if((msg.isEmpty() || msg == null) && !verify){
            return ResponseEntity.ok("Bitte Grund der Ablehnung eingeben");
        }

        String result = verificationService.verify(verifiedId, id, verify, msg);
        return ResponseEntity.ok("result");
    }

    @PutMapping("/{id}/reportDuplicate")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> reportDuplicate(@PathVariable(value = "id") Long id,
                                         @RequestParam(value = "verifiedId") Long verifiedId
    ) {
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }

        verificationService.handleDuplicate(verifiedId);

        return ResponseEntity.ok("result");
    }
}
