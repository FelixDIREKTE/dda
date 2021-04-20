package com.dd.dda.controller;


import com.dd.dda.model.FileType;
import com.dd.dda.model.Rawfile;
import com.dd.dda.service.UtilService;
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
@RequestMapping("/profilepicfiles")
public class ProfilePicFilesController {

    private final FileStorageService fileStorageService;
    private final UtilService utilService;

    public ProfilePicFilesController(FileStorageService fileStorageService, UtilService utilService) {
        this.fileStorageService = fileStorageService;
        this.utilService = utilService;
    }


    @PostMapping("{id}/upload")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity upload(@PathVariable(value = "id") Long id, @RequestParam("file") @NotNull MultipartFile file) {
        log.info("enter upload profile pic " + id);
        try {
            fileStorageService.deleteAllFiles(FileType.USERPROFILEPIC, id);
            fileStorageService.storeFile(FileType.USERPROFILEPIC, id, file);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        }
        return ResponseEntity.ok(true);
    }



    /*@GetMapping("{id}/getForOthers")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForOthers(@PathVariable(value = "id") Long id,
                                                            @RequestParam(value = "othersid") Long othersid) {


        List<Rawfile> result = fileStorageService.getFiles(FileType.USERPROFILEPIC, othersid);
        return ResponseEntity.ok(result);
    }*/

    @GetMapping("getForOthersBundle")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForOthersBundle(@RequestParam(value = "othersids") String othersids) {

        List<Long> ids = utilService.stringToArray(othersids);
        List<Rawfile> result = fileStorageService.getFilesBundle(FileType.USERPROFILEPIC, ids);
        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}/getForSelf")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Rawfile>> getForSelf(@PathVariable(value = "id") Long id)  {


        List<Rawfile> result = fileStorageService.getFiles(FileType.USERPROFILEPIC, id);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("{id}/deleteAll")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteAll(@PathVariable(value = "id") Long id)  {

        fileStorageService.deleteAllFiles(FileType.USERPROFILEPIC, id);
        return ResponseEntity.ok(true);
    }




}
