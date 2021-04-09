package com.dd.dda.controller;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Parliament;
import com.dd.dda.service.ParliamentService;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/parliaments")
public class ParliamentController {

    private final ParliamentService parliamentService;
    private final UserService userService;

    public ParliamentController(ParliamentService parliamentService, UserService userService) {
        this.parliamentService = parliamentService;
        this.userService = userService;
    }


    @GetMapping("{id}/getEligibleParliaments")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Parliament>> getEligibleParliaments(@PathVariable(value = "id") Long id) {
        log.info("enter getEligibleParliaments " + id);
        List<Parliament> result = parliamentService.getEligibleParliaments(id);
        return ResponseEntity.ok(result);
    }


    @GetMapping("{id}/getEligibleParliamentsComplete")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Parliament>> getEligibleParliamentsComplete(@PathVariable(value = "id") Long id) {
        log.info("enter getEligibleParliaments " + id);
        List<Parliament> result = parliamentService.getEligibleParliamentsComplete(id);
        return ResponseEntity.ok(result);
    }




    @GetMapping("{id}/getEligibleParliamentsFor")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Parliament>> getEligibleParliamentsFor(@PathVariable(value = "id") Long id,
                                                                      @RequestParam(value = "user_id") Long user_id) {
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }


        List<Parliament> result = parliamentService.getEligibleParliamentsComplete(user_id);
        return ResponseEntity.ok(result);
    }



    @GetMapping("{id}/getSubParliaments")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Parliament>> getSubParliaments(@PathVariable(value = "id") Long id,
                                                              @RequestParam(value = "parliament_id") Long parliament_id
    ) {
        log.info("enter getSubParliaments " + id);
        return ResponseEntity.ok(parliamentService.getSubParliaments(parliament_id));

    }

    @PutMapping("{id}/requestAccess")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> requestAccess(@PathVariable(value = "id") Long id,
                                        @RequestParam(value = "parliament_id3") Long parliament_id3,
                                        @RequestParam(value = "parliament_id4") Long parliament_id4,
                                        @RequestParam(value = "parliament_id5") Long parliament_id5,
                                        @RequestParam(value = "parliament_id6") Long parliament_id6
    ) {
        String plz = userService.getUserByIdUnencrypted(id).getZipcode();
        if(plz == null){
            return ResponseEntity.ok("Bitte zuerst PLZ angeben und Änderungen speichern");
        }
        List<Long> parliamentIds = Arrays.asList(parliament_id3, parliament_id4, parliament_id5, parliament_id6);
        if(!parliamentService.parliamentsConsistentWithZipcode(parliamentIds, plz)){
            return ResponseEntity.ok("Parlamente stimmen nicht mit PLZ überein");
        }
        if(parliamentService.sameParliaments(id, parliamentIds)){
            return ResponseEntity.ok("ok");
        }
        parliamentService.deleteVoteAccesses(id);
        parliamentService.addVoteAccesses(id, parliamentIds);
        userService.setToVerificationNeeded(id);
        return ResponseEntity.ok("ok");
    }






}
