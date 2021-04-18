package com.dd.dda.controller;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.RepresentativesBillVote;
import com.dd.dda.service.RepresentativesBillVoteService;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reprBillVotes")
public class RepresentativesBillVoteController {

    private final RepresentativesBillVoteService representativesBillVoteService;
    private final UserService userService;

    public RepresentativesBillVoteController(RepresentativesBillVoteService representativesBillVoteService, UserService userService) {
        this.representativesBillVoteService = representativesBillVoteService;
        this.userService = userService;
    }


    @GetMapping("/{id}/getVotes")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<RepresentativesBillVote>> getVotes(@PathVariable(value = "id") Long id,
                                                                  @RequestParam(value = "bill_id") Long bill_id
    ) {
        log.info("Enter getVotes");
        List<RepresentativesBillVote> result = representativesBillVoteService.getVotes(bill_id);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/{id}/create")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<RepresentativesBillVote> create(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "bill_id") Long bill_id,
                                             @RequestParam(value = "party_id") Long party_id,
                                             @RequestParam(value = "yesvotes") int yesvotes,
                                             @RequestParam(value = "novotes") int novotes,
                                             @RequestParam(value = "abstvotes") int abstvotes
                                             ) {
        log.info("Enter vote");
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }
        RepresentativesBillVote result = representativesBillVoteService.createVote(id, bill_id, party_id, yesvotes, novotes, abstvotes);
        return ResponseEntity.ok(result);
    }


}
