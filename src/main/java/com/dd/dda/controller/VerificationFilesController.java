package com.dd.dda.controller;

import com.dd.dda.model.FileType;
import com.dd.dda.model.Rawfile;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.service.UserService;
import com.dd.dda.service.file.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/verificationfiles")
public class VerificationFilesController {

    private final FileStorageService fileStorageService;
    private final UserService userService;


    public VerificationFilesController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PostMapping("{id}/upload")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity upload(@PathVariable(value = "id") Long id, @RequestParam("file") @NotNull MultipartFile file) {

        if( file.getSize() > 5000000){
            return ResponseEntity.ok("TOOBIG");
        }
        log.info("enter uploadIdentityProof " + id);
        try {
            fileStorageService.storeFile(FileType.USERVERIFICATION, id, file);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        }
        userService.updateVerificationStatusAndSave(id);
        return ResponseEntity.ok(true);
    }



    @GetMapping("{id}/getForVerification")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForVerification(@PathVariable(value = "id") Long id,
                                                          @RequestParam(value = "verifiedId") Long verifiedId) {

        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }

        List<Rawfile> result = fileStorageService.getFiles(FileType.USERVERIFICATION, verifiedId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}/getForSelf")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForSelf(@PathVariable(value = "id") Long id)  {

        List<Rawfile> result = fileStorageService.getFiles(FileType.USERVERIFICATION, id);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("{id}/deleteAll")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteAll(@PathVariable(value = "id") Long id)  {

        fileStorageService.deleteAllFiles(FileType.USERVERIFICATION, id);
        userService.updateVerificationStatusAndSave(id);
        return ResponseEntity.ok(true);
    }

}
