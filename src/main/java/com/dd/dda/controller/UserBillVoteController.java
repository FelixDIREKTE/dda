package com.dd.dda.controller;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.UserBillVote;
import com.dd.dda.service.UserBillVoteService;
import com.dd.dda.service.UserService;
import com.dd.dda.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/userBillVotes")
public class UserBillVoteController {

    private final UserBillVoteService userBillVoteService;
    private final UserService userService;
    private final UtilService utilService;

    public UserBillVoteController(UserBillVoteService userBillVoteService, UserService userService, UtilService utilService) {
        this.userBillVoteService = userBillVoteService;
        this.userService = userService;
        this.utilService = utilService;
    }


    @GetMapping("/{id}/getVotes")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Integer>> getVotes(@PathVariable(value = "id") Long id,
                                                  @RequestParam(value = "bill_id") Long bill_id
                                           ) {
        log.info("Enter getVotes");
        List<Integer> result = userBillVoteService.getVotes(bill_id);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}/getVotesAsStringBundle")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<String>> getVotesAsStringBundle(@PathVariable(value = "id") Long id,
                                                               @RequestParam(value = "bill_ids") String bill_ids
    ) {
        List<Long> ids = utilService.stringToArray(bill_ids);
        List<String> result = new ArrayList<>();
        List<Map<Object, Integer>> result0 = userBillVoteService.getVotesBundle(ids);
        for(int i = 0; i < ids.size(); i++){
            String resultPart = utilService.mapToString(result0.get(i));
            result.add(resultPart);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/getUserVote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<UserBillVote> getUserVote(@PathVariable(value = "id") Long id,
                                                    @RequestParam(value = "bill_id") Long bill_id
                                           ) {
        log.info("Enter getUserVote");
        UserBillVote result = userBillVoteService.getById(id, bill_id);
        return ResponseEntity.ok(result);

    }

    @PutMapping("/{id}/vote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<UserBillVote> vote(@PathVariable(value = "id") Long id,
                                       @RequestParam(value = "bill_id") Long bill_id,
                                       @RequestParam(value = "vote") Boolean vote
    ) {
        log.info("Enter vote");
        UserBillVote result = userBillVoteService.vote(id, bill_id, vote);
        return ResponseEntity.ok(result);

    }

    @DeleteMapping("/{id}/deleteVote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteVote(@PathVariable(value = "id") Long id,
                                       @RequestParam(value = "bill_id") Long bill_id
    ) {
        log.info("Enter deleteVote");
        userBillVoteService.deleteVote(id, bill_id);
        return ResponseEntity.ok(true);

    }



    @PutMapping("/{id}/sendResultNotification")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity sendResultNotification(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "bill_id") Long bill_id
    ) {
        log.info("Enter vote");
        if(!userService.getUserByIdUnencrypted(id).isAdmin() ){
            throw new DDAException("Keine Berechtigung!");
        }

        userBillVoteService.sendResultNotification(bill_id);
        return ResponseEntity.ok(true);

    }


}
