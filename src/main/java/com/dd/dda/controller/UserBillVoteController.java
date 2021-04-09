package com.dd.dda.controller;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.UserBillVote;
import com.dd.dda.service.UserBillVoteService;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/userBillVotes")
public class UserBillVoteController {

    private final UserBillVoteService userBillVoteService;
    private final UserService userService;

    public UserBillVoteController(UserBillVoteService userBillVoteService, UserService userService) {
        this.userBillVoteService = userBillVoteService;
        this.userService = userService;
    }


    @GetMapping("/{id}/getVotes")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Integer>> getVotes(@PathVariable(value = "id") Long id,
                                                  @RequestParam(value = "bill_id") Long bill_id
                                           ) {
        log.info("Enter getVotes");
        List<Integer> result = userBillVoteService.getVotes(bill_id);
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
