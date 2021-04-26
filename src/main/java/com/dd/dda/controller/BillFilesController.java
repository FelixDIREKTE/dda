package com.dd.dda.controller;

import com.dd.dda.model.FileType;
import com.dd.dda.model.Rawfile;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.service.BillService;
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
@RequestMapping("/billfiles")
public class BillFilesController {

    private final FileStorageService fileStorageService;
    private final BillService billService;
    private final UserService userService;


    public BillFilesController(FileStorageService fileStorageService, BillService billService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.billService = billService;
        this.userService = userService;
    }



    @PostMapping("{id}/upload")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity upload(@PathVariable(value = "id") Long id,
                                 @RequestParam(value = "bill_id") Long bill_id,
                                 @RequestParam("file") @NotNull MultipartFile file) {

        if( file.getSize() > 2000000 && !fileStorageService.isPicture(file)){
            return ResponseEntity.ok("TOOBIG");
        }

        log.info("enter upload bill files " + bill_id);

        Bill bill = billService.getBillById(bill_id);

        if(! (      id == bill.getCreated_by().getId()
                || (bill.getParliament_role() == 0 && userService.getUserByIdUnencrypted(id).isAdmin())
        )  ){
            throw  new DDAException("Keine Berechtigung!");
        }


        try {
            fileStorageService.storeFile(FileType.BILLFILES, bill_id, file);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        }
        return ResponseEntity.ok(true);
    }


    @PostMapping("{id}/downloadByUrl")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity downloadByUrl(@PathVariable(value = "id") Long id,
                                        @RequestParam(value = "bill_id") Long bill_id,
                                        @RequestParam(value = "url") String url
                                        ) {

        log.info("enter upload bill files " + bill_id);

        Bill bill = billService.getBillById(bill_id);

        if(! (      id == bill.getCreated_by().getId()
                || (bill.getParliament_role() == 0 && userService.getUserByIdUnencrypted(id).isAdmin())
        )  ){
            throw  new DDAException("Keine Berechtigung!");
        }

        try {
            fileStorageService.downloadFile(FileType.BILLFILES, bill_id, url);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        }
        return ResponseEntity.ok(true);
    }





    @GetMapping("get")
    public ResponseEntity<List<Rawfile>> getForOthers(@RequestParam(value = "bill_id") Long bill_id) {
        List<Rawfile> result = fileStorageService.getFiles(FileType.BILLFILES, bill_id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("isEmpty")
    public ResponseEntity<Boolean> isEmpty(@RequestParam(value = "bill_id") Long bill_id) {
        boolean result = fileStorageService.isEmpty(FileType.BILLFILES, bill_id);
        return ResponseEntity.ok(result);
    }




    @DeleteMapping("{id}/deleteAll")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteAll(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "bill_id") Long bill_id)  {

        Bill bill = billService.getBillById(bill_id);
        if(id != bill.getCreated_by().getId()){
            throw  new DDAException("Keine Berechtigung!");
        }

        fileStorageService.deleteAllFiles(FileType.BILLFILES, bill_id);
        return ResponseEntity.ok(true);
    }

}
