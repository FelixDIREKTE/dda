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
@RequestMapping("/parliamentpicfiles")
public class ParliamentPicFilesController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    public ParliamentPicFilesController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }



    @PostMapping("{id}/upload")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity upload(@PathVariable(value = "id") Long id,
                                 @RequestParam(value = "parliament_id") Long parliament_id,
                                 @RequestParam("file") @NotNull MultipartFile file) {

        log.info("enter upload parliament pic " + parliament_id);

        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }

        try {
            fileStorageService.deleteAllFiles(FileType.PARLIAMENTFLAG, parliament_id);
            fileStorageService.storeFile(FileType.PARLIAMENTFLAG, parliament_id, file);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        }
        return ResponseEntity.ok(true);
    }



    @GetMapping("{id}/getForOthers")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForOthers(@PathVariable(value = "id") Long id,
                                                      @RequestParam(value = "parliament_id") Long parliament_id) {


        List<Rawfile> result = fileStorageService.getFiles(FileType.PARLIAMENTFLAG, parliament_id);
        return ResponseEntity.ok(result);
    }




    @DeleteMapping("{id}/deleteAll")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteAll(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "parliament_id") Long parliament_id)  {

        fileStorageService.deleteAllFiles(FileType.PARLIAMENTFLAG, parliament_id);
        return ResponseEntity.ok(true);
    }


}

