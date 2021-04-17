package com.dd.dda.controller;

import com.dd.dda.model.FileType;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public BillController(BillService billService, UserService userService, FileStorageService fileStorageService) {
        this.billService = billService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }


    @PutMapping("/{id}/create")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Bill> createBill(    @PathVariable(value = "id") Long id,
                                                  @RequestParam(value = "title") String title,
                                                  @RequestParam(value = "abstract") String abstra,
                                                  @RequestParam(value = "parliament_id") Long parliament_id,
                                                  @RequestParam(value = "parliament_role") Integer parliament_role,
                                                  @RequestParam(value = "inputParty") String inputParty,
                                                  @RequestParam(value = "inputType") String inputType,
                                                  @RequestParam(value = "inputVorgang") String inputVorgang,
                                                  @RequestParam(value = "inputDatumVorgelegt") String inputDatumVorgelegt,
                                                  @RequestParam(value = "inputDatumAbstimm") String inputDatumAbstimm,
                                                  @RequestParam(value = "categoryBits") int categoryBits

    ) {
        log.info("enter createBill " + id);
        if(!userService.getUserByIdUnencrypted(id).isAdmin() && parliament_role == 0){
            throw new DDAException("Keine Berechtigung!");
        }


        log.info("Enter into createBill");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
         if( abstra == null || abstra.isEmpty()){
             return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
         }
        Bill result = billService.createNewBill(title, abstra, id, parliament_id, parliament_role, inputParty, inputType, inputVorgang, inputDatumVorgelegt, inputDatumAbstimm, categoryBits);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/{id}/updateData")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> updateBillData(@PathVariable(value = "id") Long id,
                                                 @RequestParam(value = "title") String title,
                                                 @RequestParam(value = "abstract") String abstr,
                                                 @RequestParam(value = "bill_id") Long bill_id,
                                                 @RequestParam(value = "inputParty") String inputParty,
                                                 @RequestParam(value = "inputType") String inputType,
                                                 @RequestParam(value = "inputVorgang") String inputVorgang,
                                                 @RequestParam(value = "inputDatumVorgelegt") String inputDatumVorgelegt,
                                                 @RequestParam(value = "inputDatumAbstimm") String inputDatumAbstimm,
                                                 @RequestParam(value = "categoryBits") int categoryBits
                                                 ) {
        log.info("enter updateBillData " + id);

        String result = billService.updateBill(title, abstr, bill_id, id, inputParty, inputType, inputVorgang, inputDatumVorgelegt, inputDatumAbstimm, categoryBits);
        return ResponseEntity.ok(result);
    }



    @GetMapping("{id}/getRankedBills")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Bill>> getRankedBills(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "parliament_id") long parliament_id,
                                                     @RequestParam(value = "parliament_role") int parliament_role

    ) {
        log.info("enter getRankedBills " + id);
        return ResponseEntity.ok(billService.getRankedBills(id, parliament_id, parliament_role));
    }


    @GetMapping("{id}/getBillSearch")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Bill>> getBillSearch(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "parliament_id") long parliament_id,
                                                    @RequestParam(value = "parliament_role") Long parliament_role,
                                                     @RequestParam(value = "searchterm")  String searchterm

    ) {
        log.info("enter getBills " + id);
        return ResponseEntity.ok(billService.getBillSearch(id, parliament_id, parliament_role, searchterm));
    }





    @GetMapping("{id}/getBill")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Bill> getBill(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "bill_id") Long bill_id
    ) {
        log.info("enter getBill " + id);
        return ResponseEntity.ok(billService.getBill(bill_id));
    }



    @DeleteMapping("{id}/delete")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> delete(@PathVariable(value = "id") Long id,
                                          @RequestParam(value = "bill_id") Long bill_id)  {

        log.info("enter createBill " + id);
        Bill bill = billService.getBillById(bill_id);
        if(! (      id == bill.getCreated_by().getId()
                || (bill.getParliament_role() == 0 && userService.getUserByIdUnencrypted(id).isAdmin())
        )  ){
            throw  new DDAException("Keine Berechtigung!");
        }

        fileStorageService.deleteAllFiles(FileType.BILLFILES, bill_id);
        billService.deleteBill(id, bill_id);
        return ResponseEntity.ok(true);
    }



    @PutMapping("/{id}/saveReadBills")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity saveReadBills(    @PathVariable(value = "id") Long id,
                                            @RequestParam(value = "readBillsIds") Long[] readbillsIds,
                                               @RequestParam(value = "readBillDetailId") Long readBillDetailId
    ) {

        userService.addCommentsRead(id, readbillsIds.length);
        billService.addReads(readbillsIds, readBillDetailId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("{id}/loadReadBillsIds")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Long>> loadReadBillsIds(@PathVariable(value = "id") Long id,
                                                       @RequestParam(value = "parliament_id") Long parliament_id,
                                                          @RequestParam(value = "parliament_role") int parliament_role
    ) {
        log.info("enter loadReadBillsIds " + id);
        List<Long> ratedbillsids = billService.getBillIdsOfRatingsForParliament(id, parliament_id, parliament_role );
        return ResponseEntity.ok(ratedbillsids);
    }

}
